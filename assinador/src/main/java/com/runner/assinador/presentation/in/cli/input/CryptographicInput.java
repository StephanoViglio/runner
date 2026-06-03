package com.runner.assinador.presentation.in.cli.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.runner.assinador.domain.model.CryptographicStrategy;

public class CryptographicInput {

    private final CryptographicStrategy cryptographicStrategy;
    private final String pin;
    private final String identifier;
    private final Integer slotId;
    private final String tokenLabel;

    @JsonCreator
    public CryptographicInput(
            @JsonProperty("cryptographicStrategy") CryptographicStrategy cryptographicStrategy,
            @JsonProperty("pin") String pin,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("slotId") Integer slotId,
            @JsonProperty("tokenLabel") String tokenLabel) {
        this.cryptographicStrategy = cryptographicStrategy;
        this.pin = pin;
        this.identifier = identifier;
        this.slotId = slotId;
        this.tokenLabel = tokenLabel;
    }

    public CryptographicStrategy getCryptographicStrategy() { return cryptographicStrategy; }
    public String getPin() { return pin; }
    public String getIdentifier() { return identifier; }
    public Integer getSlotId() { return slotId; }
    public String getTokenLabel() { return tokenLabel; }
}