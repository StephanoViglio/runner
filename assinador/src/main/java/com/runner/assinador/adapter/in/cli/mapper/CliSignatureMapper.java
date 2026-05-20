package com.runner.assinador.adapter.in.cli.mapper;

import com.runner.assinador.adapter.in.cli.input.SignInput;
import com.runner.assinador.adapter.in.cli.input.VerifyInput;
import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;

public class CliSignatureMapper {

    public SignDocumentCommand toSignCommand(SignInput input) {
        BundleData bundle = new BundleData(
                input.getBundle().getEntry().stream()
                        .map(e -> new ResourceEntry(e.getFullUrl(), e.getResourceJson()))
                        .toList()
        );

        ProvenanceData provenance = new ProvenanceData(input.getProvenance().getTarget());

        return new SignDocumentCommand(
                bundle,
                provenance,
                input.getCryptographicMaterial().getCryptographicStrategy(),
                input.getCryptographicMaterial().getPin(),
                input.getCryptographicMaterial().getIdentifier(),
                input.getCryptographicMaterial().getSlotId(),
                input.getCryptographicMaterial().getTokenLabel(),
                input.getCertificateChain(),
                input.getReferenceTimestamp(),
                input.getTimestampStrategy(),
                input.getPolicyUri()
        );
    }

    public VerifySignatureCommand toVerifyCommand(VerifyInput input) {
        BundleData bundle = input.getBundle() == null ? null : new BundleData(
                input.getBundle().getEntry().stream()
                        .map(e -> new ResourceEntry(e.getFullUrl(), e.getResourceJson()))
                        .toList()
        );

        ProvenanceData provenance = input.getProvenance() == null ? null
                : new ProvenanceData(input.getProvenance().getTarget());

        return new VerifySignatureCommand(
                input.getSignatureData(),
                input.getReferenceTimestamp(),
                input.getPolicyUri(),
                bundle,
                provenance
        );
    }
}