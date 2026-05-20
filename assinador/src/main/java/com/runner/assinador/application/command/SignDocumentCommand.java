package com.runner.assinador.application.command;

import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.CryptographicStrategy;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.TimestampStrategy;

import java.util.List;

public class SignDocumentCommand {

    private final BundleData bundle;
    private final ProvenanceData provenance;
    private final CryptographicStrategy cryptographicStrategy;
    private final String pin;
    private final String identifier;
    private final Integer slotId;
    private final String tokenLabel;
    private final List<String> certificateChain;
    private final Long referenceTimestamp;
    private final TimestampStrategy timestampStrategy;
    private final String policyUri;

    public SignDocumentCommand(BundleData bundle, ProvenanceData provenance, CryptographicStrategy cryptographicStrategy,
                               String pin, String identifier, Integer slotId, String tokenLabel, List<String> certificateChain,
                               Long referenceTimestamp, TimestampStrategy timestampStrategy, String policyUri) {
        if (bundle == null)
            throw new IllegalArgumentException("bundle é obrigatório");
        if (provenance == null)
            throw new IllegalArgumentException("provenance é obrigatório");
        if (cryptographicStrategy == null)
            throw new IllegalArgumentException("cryptographicStrategy é obrigatória");
        if (pin == null || pin.isBlank())
            throw new IllegalArgumentException("pin é obrigatório");
        if (identifier == null || identifier.isBlank())
            throw new IllegalArgumentException("identifier é obrigatório");
        if (certificateChain == null || certificateChain.isEmpty())
            throw new IllegalArgumentException("certificateChain deve ter ao menos um certificado");
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
        this.cryptographicStrategy = cryptographicStrategy;
        this.pin = pin;
        this.identifier = identifier;
        this.slotId = slotId;
        this.tokenLabel = tokenLabel;
        this.certificateChain = List.copyOf(certificateChain);
        this.referenceTimestamp = referenceTimestamp;
        this.timestampStrategy = timestampStrategy;
        this.policyUri = policyUri;
    }

    public BundleData getBundle() { return bundle; }
    public ProvenanceData getProvenance() { return provenance; }
    public CryptographicStrategy getCryptographicStrategy() { return cryptographicStrategy; }
    public String getPin() { return pin; }
    public String getIdentifier() { return identifier; }
    public Integer getSlotId() { return slotId; }
    public String getTokenLabel() { return tokenLabel; }
    public List<String> getCertificateChain() { return certificateChain; }
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public TimestampStrategy getTimestampStrategy() { return timestampStrategy; }
    public String getPolicyUri() { return policyUri; }
}