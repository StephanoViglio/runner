package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyInput {

    @JsonProperty("signatureData")
    private String signatureData;

    @JsonProperty("referenceTimestamp")
    private Long referenceTimestamp;

    @JsonProperty("policyUri")
    private String policyUri;

    @JsonProperty("bundle")
    private SignInput.BundleInput bundle;

    @JsonProperty("provenance")
    private SignInput.ProvenanceInput provenance;

    public String getSignatureData() { return signatureData; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public String getPolicyUri() { return policyUri; }
    public SignInput.BundleInput getBundle() { return bundle; }
    public SignInput.ProvenanceInput getProvenance() { return provenance; }
}