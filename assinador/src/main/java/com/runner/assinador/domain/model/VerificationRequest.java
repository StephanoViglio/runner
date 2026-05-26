package com.runner.assinador.domain.model;

public class VerificationRequest {

    private final String signatureData;
    private final long referenceTimestamp;
    private final String policyUri;
    private final BundleData bundle;
    private final ProvenanceData provenance;

    public VerificationRequest(String signatureData,
                               long referenceTimestamp,
                               String policyUri,
                               BundleData bundle,
                               ProvenanceData provenance) {
        if (signatureData == null || signatureData.isBlank())
            throw new IllegalArgumentException("signatureData é obrigatório");
        if (policyUri == null || policyUri.isBlank())
            throw new IllegalArgumentException("policyUri é obrigatório");
        if ((bundle == null) != (provenance == null))
            throw new IllegalArgumentException("bundle e provenance devem ser fornecidos em conjunto");

        this.signatureData = signatureData;
        this.referenceTimestamp = referenceTimestamp;
        this.policyUri = policyUri;
        this.bundle = bundle;
        this.provenance = provenance;
    }

    public String getSignatureData() { return signatureData; }
    public long getReferenceTimestamp() { return referenceTimestamp; }
    public String getPolicyUri() { return policyUri; }
    public BundleData getBundle() { return bundle; }
    public ProvenanceData getProvenance() { return provenance; }
    public boolean hasIntegrityCheck() { return bundle != null; }
}