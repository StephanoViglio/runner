package com.runner.assinador.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.dto.request.ResourceEntryDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.exception.SignatureException;
import com.runner.assinador.utils.OperationOutcomeCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignRequestValidator {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;

    private final ObjectMapper objectMapper;

    public void validate(SignRequestDTO request) {
        log.debug("Iniciando validação do SignRequestDTO");
        validateTimestampWindow(request.getReferenceTimestamp());
        validateBundleEntries(request);
        validateProvenanceTargets(request);
        log.debug("Validação do SignRequestDTO concluída com sucesso");
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new SignatureException(
                    OperationOutcomeCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW,
                    String.format(
                            "referenceTimestamp (%d) fora da janela de tolerância de ±%d segundos " +
                            "em relação ao servidor (%d). Diferença: %d segundos.",
                            referenceTimestamp, TIMESTAMP_TOLERANCE_SECONDS, now, diff));
        }
    }

    private void validateBundleEntries(SignRequestDTO request) {
        Set<String> seen = new HashSet<>();
        for (ResourceEntryDTO entry : request.getBundle().getEntry()) {
            if (!seen.add(entry.getFullUrl())) {
                throw new SignatureException(
                        OperationOutcomeCode.FORMAT_DUPLICATE_FULLURL,
                        "bundle.entry contém fullUrl duplicado: '" + entry.getFullUrl() + "'.");
            }
        }
    }

    private void validateProvenanceTargets(SignRequestDTO request) {
        Map<String, ResourceEntryDTO> entryByFullUrl = request.getBundle().getEntry().stream()
                .collect(Collectors.toMap(ResourceEntryDTO::getFullUrl, e -> e));

        Set<String> seen = new HashSet<>();
        for (String ref : request.getProvenance().getTarget()) {

            if (!seen.add(ref)) {
                throw new SignatureException(
                        OperationOutcomeCode.FORMAT_PROVENANCE_TARGET_DUPLICATE,
                        "provenance.target contém referência duplicada: '" + ref + "'.");
            }

            ResourceEntryDTO entry = entryByFullUrl.get(ref);
            if (entry == null) {
                throw new SignatureException(
                        OperationOutcomeCode.FORMAT_TARGET_REFERENCE_MISSING,
                        "provenance.target referencia '" + ref +
                        "', mas nenhuma entry em bundle.entry possui esse fullUrl.");
            }

            if (entry.getResourceJson() == null || entry.getResourceJson().isBlank()) {
                throw new SignatureException(
                        OperationOutcomeCode.FORMAT_BUNDLE_RESOURCE_MISSING,
                        "A entry com fullUrl '" + ref +
                        "' está referenciada em provenance.target mas não possui resourceJson.");
            }

            validateResourceJson(ref, entry.getResourceJson());
        }
    }

    private void validateResourceJson(String fullUrl, String resourceJson) {
        try {
            objectMapper.readTree(resourceJson);
        } catch (Exception e) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "O resourceJson da entry '" + fullUrl +
                    "' não é um JSON válido: " + e.getMessage());
        }
    }
}