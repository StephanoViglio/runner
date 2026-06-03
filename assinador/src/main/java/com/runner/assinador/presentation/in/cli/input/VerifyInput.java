package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyInput {

    private final String signatureData;
    private final Long referenceTimestamp;
    private final String policyUri;
    private final BundleInput bundle;
    private final ProvenanceInput provenance;

    @JsonCreator
    public VerifyInput(
            @JsonProperty("signatureData") String signatureData,
            @JsonProperty("referenceTimestamp") Long referenceTimestamp,
            @JsonProperty("policyUri") String policyUri,
            @JsonProperty("bundle") BundleInput bundle,
            @JsonProperty("provenance") ProvenanceInput provenance) {
        this.signatureData = signatureData;
        this.referenceTimestamp = referenceTimestamp;
        this.policyUri = policyUri;
        this.bundle = bundle;
        this.provenance = provenance;
    }

    public String getSignatureData() { return signatureData; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public String getPolicyUri() { return policyUri; }
    public BundleInput getBundle() { return bundle; }
    public ProvenanceInput getProvenance() { return provenance; }
}