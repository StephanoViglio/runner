package com.runner.assinador.domain.port.in;

import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.CryptographicMaterial;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.TimestampStrategy;

public class SignDocumentCommand {

    private final BundleData bundle;
    private final ProvenanceData provenance;
    private final CryptographicMaterial cryptographicMaterial;
    private final Long referenceTimestamp;
    private final TimestampStrategy timestampStrategy;
    private final String policyUri;

    public SignDocumentCommand(BundleData bundle,
                               ProvenanceData provenance,
                               CryptographicMaterial cryptographicMaterial,
                               Long referenceTimestamp,
                               TimestampStrategy timestampStrategy,
                               String policyUri) {
        if (bundle == null)
            throw new IllegalArgumentException("bundle é obrigatório");
        if (provenance == null)
            throw new IllegalArgumentException("provenance é obrigatório");
        if (cryptographicMaterial == null)
            throw new IllegalArgumentException("cryptographicMaterial é obrigatório");
        if (referenceTimestamp == null)
            throw new IllegalArgumentException("referenceTimestamp é obrigatório");
        if (referenceTimestamp < 1751328000L)
            throw new IllegalArgumentException("referenceTimestamp abaixo do mínimo permitido (01/07/2025)");
        if (referenceTimestamp > 4102444800L)
            throw new IllegalArgumentException("referenceTimestamp acima do máximo permitido (31/12/2099)");
        if (timestampStrategy == null)
            throw new IllegalArgumentException("timestampStrategy é obrigatória");
        if (policyUri == null || !policyUri.matches("^https://.+\\|\\d+\\.\\d+\\.\\d+$"))
            throw new IllegalArgumentException("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");

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
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public TimestampStrategy getTimestampStrategy() { return timestampStrategy; }
    public String getPolicyUri() { return policyUri; }
}