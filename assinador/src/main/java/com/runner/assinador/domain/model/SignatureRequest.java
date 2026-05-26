package com.runner.assinador.domain.model;

public class SignatureRequest {

    private final BundleData bundle;
    private final ProvenanceData provenance;
    private final CryptographicMaterial cryptographicMaterial;
    private final long referenceTimestamp;
    private final TimestampStrategy timestampStrategy;
    private final String policyUri;

    public SignatureRequest(BundleData bundle,
                            ProvenanceData provenance,
                            CryptographicMaterial cryptographicMaterial,
                            long referenceTimestamp,
                            TimestampStrategy timestampStrategy,
                            String policyUri) {
        if (bundle == null)
            throw new IllegalArgumentException("bundle é obrigatório");
        if (provenance == null)
            throw new IllegalArgumentException("provenance é obrigatório");
        if (cryptographicMaterial == null)
            throw new IllegalArgumentException("cryptographicMaterial é obrigatório");
        if (timestampStrategy == null)
            throw new IllegalArgumentException("timestampStrategy é obrigatória");
        if (policyUri == null || policyUri.isBlank())
            throw new IllegalArgumentException("policyUri é obrigatório");

        this.bundle = bundle;
        this.provenance = provenance;
        this.cryptographicMaterial = cryptographicMaterial;
        this.referenceTimestamp = referenceTimestamp;
        this.timestampStrategy = timestampStrategy;
        this.policyUri = policyUri;
    }

    public BundleData getBundle() { return bundle; }
    public ProvenanceData getProvenance() { return provenance; }
    public CryptographicMaterial getCryptographicMaterial() { return cryptographicMaterial; }
    public long getReferenceTimestamp() { return referenceTimestamp; }
    public TimestampStrategy getTimestampStrategy() { return timestampStrategy; }
    public String getPolicyUri() { return policyUri; }
}