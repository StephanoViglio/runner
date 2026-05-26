package com.runner.assinador.domain.port.in;

import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;

public class VerifySignatureCommand {

    private final String signatureData;
    private final Long referenceTimestamp;
    private final String policyUri;
    private final BundleData bundle;
    private final ProvenanceData provenance;

    public VerifySignatureCommand(String signatureData, Long referenceTimestamp, String policyUri, BundleData bundle,
                                  ProvenanceData provenance) {

        if (signatureData == null || signatureData.isBlank())
            throw new IllegalArgumentException("signatureData é obrigatório");
        if (referenceTimestamp == null)
            throw new IllegalArgumentException("referenceTimestamp é obrigatório");
        if (referenceTimestamp < 1751328000L)
            throw new IllegalArgumentException("referenceTimestamp abaixo do mínimo permitido (01/07/2025)");
        if (referenceTimestamp > 4102444800L)
            throw new IllegalArgumentException("referenceTimestamp acima do máximo permitido (31/12/2099)");
        if (policyUri == null || !policyUri.matches("^https://.+\\|\\d+\\.\\d+\\.\\d+$"))
            throw new IllegalArgumentException("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");

        if ((bundle == null) != (provenance == null))
            throw new IllegalArgumentException("bundle e provenance devem ser fornecidos em conjunto");

        this.signatureData = signatureData;
        this.referenceTimestamp = referenceTimestamp;
        this.policyUri = policyUri;
        this.bundle = bundle;
        this.provenance = provenance;
    }

    public String getSignatureData() { return signatureData; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public String getPolicyUri() { return policyUri; }
    public BundleData getBundle() { return bundle; }
    public ProvenanceData getProvenance() { return provenance; }
    public boolean hasIntegrityCheck() { return bundle != null; }
}