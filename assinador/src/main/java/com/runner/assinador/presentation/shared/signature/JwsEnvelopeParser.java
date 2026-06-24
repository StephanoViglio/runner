package com.runner.assinador.presentation.shared.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;

import java.util.Base64;
import java.util.Set;

public final class JwsEnvelopeParser {

    private static final Set<String> SUPPORTED_ALGS = Set.of("RS256", "ES256");
    private static final String PROTECTED = "protected";

    private JwsEnvelopeParser() {}

    public static JwsEnvelope parse(ObjectMapper objectMapper, String signatureData) {
        JsonNode jws = decodeAndParseJws(objectMapper, signatureData);
        JsonNode sig0 = validateJwsStructure(jws);
        String protectedHeaderB64Url = sig0.get(PROTECTED).asText();
        JsonNode protectedHeader = decodeProtectedHeader(objectMapper, protectedHeaderB64Url);

        return new JwsEnvelope(
                jws.get("payload").asText(),
                protectedHeader,
                protectedHeaderB64Url,
                sig0.get("signature").asText());
    }

    public static void validateProtectedHeader(JsonNode protectedHeader) {
        String alg = protectedHeader.path("alg").asText(null);
        if (alg == null) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: campo 'alg' ausente.");
        }
        if (!SUPPORTED_ALGS.contains(alg)) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: algoritmo '" + alg +
                            "' não suportado. Aceitos: " + SUPPORTED_ALGS + ".");
        }

        JsonNode x5c = protectedHeader.get("x5c");
        if (x5c == null || !x5c.isArray() || x5c.isEmpty()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: campo 'x5c' ausente ou vazio.");
        }

        JsonNode sigPId = protectedHeader.get("sigPId");
        if (sigPId == null || !sigPId.has("id") || sigPId.get("id").isNull()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: campo 'sigPId.id' ausente ou nulo.");
        }
    }

    private static JsonNode decodeAndParseJws(ObjectMapper objectMapper, String signatureData) {
        byte[] jwsBytes;
        try {
            jwsBytes = Base64.getDecoder().decode(signatureData);
        } catch (IllegalArgumentException e) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_BASE64_INVALID,
                    "signatureData não é base64 padrão válido (RFC 4648): " + e.getMessage());
        }
        try {
            return objectMapper.readTree(jwsBytes);
        } catch (Exception e) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JSON_MALFORMED,
                    "Conteúdo decodificado de signatureData não é JSON válido: " + e.getMessage());
        }
    }

    private static JsonNode validateJwsStructure(JsonNode jws) {
        if (!jws.has("payload") || jws.get("payload").isNull()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS: propriedade 'payload' ausente ou nula.");
        }

        JsonNode signatures = jws.get("signatures");
        if (signatures == null || !signatures.isArray() || signatures.isEmpty()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS: propriedade 'signatures' ausente, não é array ou está vazia.");
        }

        JsonNode sig0 = signatures.get(0);
        if (!sig0.has(PROTECTED) || sig0.get(PROTECTED).isNull()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS: signatures[0].protected ausente ou nulo.");
        }
        if (!sig0.has("signature") || sig0.get("signature").isNull()) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS: signatures[0].signature ausente ou nulo.");
        }

        return sig0;
    }

    private static JsonNode decodeProtectedHeader(ObjectMapper objectMapper, String protectedB64Url) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(protectedB64Url);
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "Não foi possível decodificar o protected header base64Url: " + e.getMessage());
        }
    }
}
