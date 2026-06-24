package com.runner.assinador.presentation.shared.signature;

import com.fasterxml.jackson.databind.JsonNode;

public record JwsEnvelope(
        String payload,
        JsonNode protectedHeader,
        String protectedHeaderB64Url,
        String signatureB64Url) {
}
