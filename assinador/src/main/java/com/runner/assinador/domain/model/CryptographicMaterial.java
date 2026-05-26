package com.runner.assinador.domain.model;

import java.util.List;

public class CryptographicMaterial {

    private final CryptographicStrategy strategy;
    private final String pin;
    private final String identifier;
    private final Integer slotId;
    private final String tokenLabel;
    private final List<String> certificateChain;

    public CryptographicMaterial(CryptographicStrategy strategy,
                                 String pin,
                                 String identifier,
                                 Integer slotId,
                                 String tokenLabel,
                                 List<String> certificateChain) {
        if (strategy == null)
            throw new IllegalArgumentException("cryptographicStrategy é obrigatória");
        if (pin == null || pin.isBlank())
            throw new IllegalArgumentException("pin é obrigatório");
        if (identifier == null || identifier.isBlank())
            throw new IllegalArgumentException("identifier é obrigatório");
        if (certificateChain == null || certificateChain.isEmpty())
            throw new IllegalArgumentException("certificateChain deve ter ao menos um certificado");

        this.strategy = strategy;
        this.pin = pin;
        this.identifier = identifier;
        this.slotId = slotId;
        this.tokenLabel = tokenLabel;
        this.certificateChain = List.copyOf(certificateChain);
    }

    public CryptographicStrategy getStrategy() { return strategy; }
    public String getPin() { return pin; }
    public String getIdentifier() { return identifier; }
    public Integer getSlotId() { return slotId; }
    public String getTokenLabel() { return tokenLabel; }
    public List<String> getCertificateChain() { return certificateChain; }
}