package com.runner.assinador.application.service;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.SignDocumentUseCase;
import com.runner.assinador.domain.port.out.SignatureProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignDocumentService implements SignDocumentUseCase {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;

    private final SignatureProvider signatureProvider;

    @Override
    public SignatureResult execute(SignDocumentCommand command) {
        log.info("SignDocumentService.execute() — iniciando validações");

        validateTimestampWindow(command.getReferenceTimestamp());
        validateBundleEntries(command.getBundle());
        validateProvenanceTargets(command.getBundle(), command.getProvenance());

        SignatureRequest request = new SignatureRequest(
                command.getBundle(),
                command.getProvenance(),
                command.getCryptographicMaterial(),
                command.getReferenceTimestamp(),
                command.getTimestampStrategy(),
                command.getPolicyUri()
        );

        log.info("Validações concluídas — delegando para SignatureProvider. strategy={}",
                command.getTimestampStrategy());

        return signatureProvider.sign(request);
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now  = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new SignatureException(
                    DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW,
                    String.format(
                            "referenceTimestamp (%d) fora da janela de tolerância de ±%d segundos " +
                                    "em relação ao servidor (%d). Diferença: %d segundos.",
                            referenceTimestamp, TIMESTAMP_TOLERANCE_SECONDS, now, diff));
        }
    }

    private void validateBundleEntries(BundleData bundle) {
        Set<String> seen = new HashSet<>();
        for (ResourceEntry entry : bundle.getEntries()) {
            if (!seen.add(entry.getFullUrl())) {
                throw new SignatureException(
                        DomainErrorCode.FORMAT_DUPLICATE_FULLURL,
                        "bundle.entry contém fullUrl duplicado: '" + entry.getFullUrl() + "'.");
            }
        }
    }

    private void validateProvenanceTargets(BundleData bundle, ProvenanceData provenance) {
        Map<String, ResourceEntry> entryByFullUrl = bundle.getEntries().stream()
                .collect(Collectors.toMap(ResourceEntry::getFullUrl, e -> e));

        Set<String> seen = new HashSet<>();
        for (String ref : provenance.getTargets()) {
            if (!seen.add(ref)) {
                throw new SignatureException(
                        DomainErrorCode.FORMAT_PROVENANCE_TARGET_DUPLICATE,
                        "provenance.target contém referência duplicada: '" + ref + "'.");
            }

            ResourceEntry entry = entryByFullUrl.get(ref);
            if (entry == null) {
                throw new SignatureException(
                        DomainErrorCode.FORMAT_TARGET_REFERENCE_MISSING,
                        "provenance.target referencia '" + ref +
                                "', mas nenhuma entry em bundle.entry possui esse fullUrl.");
            }
        }
    }
}