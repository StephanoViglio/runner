package com.runner.assinador.adapter.in.rest.mapper;

import com.runner.assinador.adapter.in.rest.dto.request.ResourceEntryDTO;
import com.runner.assinador.adapter.in.rest.dto.request.SignRequestDTO;
import com.runner.assinador.adapter.in.rest.dto.request.VerifyRequestDTO;
import com.runner.assinador.adapter.in.rest.dto.response.*;
import com.runner.assinador.adapter.shared.factory.OperationOutcomeFactory;
import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestSignatureMapper {

    public SignDocumentCommand toSignCommand(SignRequestDTO dto) {
        BundleData bundle = new BundleData(
                dto.getBundle().getEntry().stream()
                        .map(e -> new ResourceEntry(e.getFullUrl(), e.getResourceJson()))
                        .toList()
        );

        ProvenanceData provenance = new ProvenanceData(dto.getProvenance().getTarget());

        return new SignDocumentCommand(
                bundle,
                provenance,
                dto.getCryptographicMaterial().getCryptographicStrategy(),
                dto.getCryptographicMaterial().getPin(),
                dto.getCryptographicMaterial().getIdentifier(),
                dto.getCryptographicMaterial().getSlotId(),
                dto.getCryptographicMaterial().getTokenLabel(),
                dto.getCertificateChain(),
                dto.getReferenceTimestamp(),
                dto.getTimestampStrategy(),
                dto.getPolicyUri()
        );
    }

    public VerifySignatureCommand toVerifyCommand(VerifyRequestDTO dto) {
        BundleData bundle       = dto.getBundle() == null ? null : new BundleData(
                dto.getBundle().getEntry().stream()
                        .map(e -> new ResourceEntry(e.getFullUrl(), e.getResourceJson()))
                        .toList()
        );

        ProvenanceData provenance = dto.getProvenance() == null ? null
                : new ProvenanceData(dto.getProvenance().getTarget());

        return new VerifySignatureCommand(
                dto.getSignatureData(),
                dto.getReferenceTimestamp(),
                dto.getPolicyUri(),
                bundle,
                provenance
        );
    }

    public SignResponseDTO toSignResponse(SignatureResult result) {
        List<SignatureCodingDTO> type = result.getType().stream()
                .map(c -> new SignatureCodingDTO(c.system(), c.code()))
                .toList();

        SignatureWhoDTO who = new SignatureWhoDTO(
                new SignatureIdentifierDTO("urn:brasil:cpf", result.getSignerCpf())
        );

        return new SignResponseDTO(type, result.getWhen(), who,
                result.getTargetFormat(), result.getSigFormat(), result.getData());
    }

    public OperationOutcomeDTO toOperationOutcome(VerificationResult result) {
        return OperationOutcomeFactory.fromVerificationResult(result);
    }
}