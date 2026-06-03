package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.runner.assinador.domain.model.TimestampStrategy;

import java.util.List;

public class SignInput {

    private final BundleInput bundle;
    private final ProvenanceInput provenance;
    private final CryptographicInput cryptographicMaterial;
    private final List<String> certificateChain;
    private final Long referenceTimestamp;
    private final TimestampStrategy timestampStrategy;
    private final String policyUri;

    @JsonCreator
    public SignInput(
            @JsonProperty("bundle") BundleInput bundle,
            @JsonProperty("provenance") ProvenanceInput provenance,
            @JsonProperty("cryptographicMaterial") CryptographicInput cryptographicMaterial,
            @JsonProperty("certificateChain") List<String> certificateChain,
            @JsonProperty("referenceTimestamp") Long referenceTimestamp,
            @JsonProperty("timestampStrategy") TimestampStrategy timestampStrategy,
            @JsonProperty("policyUri") String policyUri) {
        this.bundle = bundle;
        this.provenance = provenance;
        this.cryptographicMaterial = cryptographicMaterial;
        this.certificateChain = certificateChain;
        this.referenceTimestamp = referenceTimestamp;
        this.timestampStrategy = timestampStrategy;
        this.policyUri = policyUri;
    }

    public BundleInput getBundle() { return bundle; }
    public ProvenanceInput getProvenance() { return provenance; }
    public CryptographicInput getCryptographicMaterial() { return cryptographicMaterial; }
    public List<String> getCertificateChain() { return certificateChain; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public TimestampStrategy getTimestampStrategy() { return timestampStrategy; }
    public String getPolicyUri() { return policyUri; }
}