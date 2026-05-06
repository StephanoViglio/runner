package com.runner.assinador.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.dto.request.ResourceEntryDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.*;
import com.runner.assinador.exception.SignatureException;
import com.runner.assinador.service.SignatureService;
import com.runner.assinador.utils.OperationOutcomeCode;
import com.runner.assinador.utils.TimestampStrategy;
import com.runner.assinador.validation.SignRequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakeSignatureService implements SignatureService {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;

    private static final String FAKE_CERT_DER_BASE64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAfake";
    private static final String FAKE_CPF              = "98765432100";
    private static final String SIG_TYPE_SYSTEM       = "urn:iso-astm:E1762-95:2013";
    private static final String SIG_TYPE_CODE_AUTORIA = "1.2.840.10065.1.12.1.1";
    private static final String WHO_SYSTEM_CPF        = "urn:brasil:cpf";
    private static final String TARGET_FORMAT         = "application/octet-stream";
    private static final String SIG_FORMAT            = "application/jose";
    private static final String PROTECTED             = "protected";

    private final ObjectMapper objectMapper;
    private final SignRequestValidator signRequestValidator;

    @Override
    public SignResponseDTO sign(SignRequestDTO request) {
        log.info("[FAKE] sign() — iniciando validação das entradas");

        signRequestValidator.validate(request);

        log.info("[FAKE] Entradas válidas — construindo Signature FHIR. strategy={}",
                request.getTimestampStrategy());

        String jwsJson       = buildFakeJwsJson(request);
        String signatureData = encodeToBase64Standard(jwsJson);
        String when          = Instant.ofEpochSecond(request.getReferenceTimestamp()).toString();

        return SignResponseDTO.builder()
                .type(List.of(new SignatureCodingDTO(SIG_TYPE_SYSTEM, SIG_TYPE_CODE_AUTORIA)))
                .when(when)
                .who(new SignatureWhoDTO(new SignatureIdentifierDTO(WHO_SYSTEM_CPF, FAKE_CPF)))
                .targetFormat(TARGET_FORMAT)
                .sigFormat(SIG_FORMAT)
                .data(signatureData)
                .build();
    }

    @Override
    public VerifyResponseDTO verify(VerifyRequestDTO request) {
        log.info("[FAKE] verify() — iniciando validação das entradas e estrutura JWS");

        validateTimestampWindow(request.getReferenceTimestamp());

        JsonNode jws = decodeAndParseJws(request.getSignatureData());
        validateJwsStructure(jws);

        if (request.getBundle() != null && request.getProvenance() != null) {
            log.info("[FAKE] Bundle e Provenance fornecidos — executando verificação de integridade");
            validateIntegrityCheck(request);
        } else if (request.getBundle() != null || request.getProvenance() != null) {
            log.warn("[FAKE] bundle e provenance devem ser fornecidos em conjunto " +
                    "para verificação de integridade — ignorando verificação parcial");
        }

        log.info("[FAKE] JWS válido — retornando resultado de sucesso");

        return VerifyResponseDTO.builder()
                .valid(true)
                .build();
    }

    private String buildFakeJwsJson(SignRequestDTO request) {
        String protectedHeader = buildFakeProtectedHeader(request);
        String payload         = buildFakePayload(request);
        String header          = buildFakeUnprotectedHeader(request.getTimestampStrategy());

        return String.format(
                "{\"payload\":\"%s\",\"signatures\":[{\"protected\":\"%s\"," +
                        "\"header\":%s,\"signature\":\"%s\"}]}",
                payload,
                protectedHeader,
                header,
                "FAKE_PKCS11_SIGNATURE_BASE64URL");
    }

    private String buildFakeProtectedHeader(SignRequestDTO request) {
        String headerJson = request.getTimestampStrategy() == TimestampStrategy.IAT
                ? String.format(
                "{\"alg\":\"RS256\",\"x5c\":[\"%s\"],\"iat\":%d," +
                "\"sigPId\":{\"id\":\"%s\"}}",
                FAKE_CERT_DER_BASE64,
                request.getReferenceTimestamp(),
                request.getPolicyUri())
                : String.format(
                "{\"alg\":\"RS256\",\"x5c\":[\"%s\"]," +
                "\"sigPId\":{\"id\":\"%s\"}}",
                FAKE_CERT_DER_BASE64,
                request.getPolicyUri());

        return toBase64Url(headerJson);
    }

    private String buildFakePayload(SignRequestDTO request) {
        Map<String, ResourceEntryDTO> entryByFullUrl = request.getBundle().getEntry().stream()
                .collect(Collectors.toMap(ResourceEntryDTO::getFullUrl, e -> e));

        StringBuilder concatenated = new StringBuilder();
        for (String ref : request.getProvenance().getTarget()) {
            concatenated.append(entryByFullUrl.get(ref).getResourceJson());
        }

        byte[] sha256 = sha256(concatenated.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256);
    }

    private String buildFakeUnprotectedHeader(TimestampStrategy strategy) {
        String fakeDigestAlg  = "http://www.w3.org/2001/04/xmlenc#sha512";
        String fakeOcspDigest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        return strategy == TimestampStrategy.TSA
                ? String.format(
                "{\"sigTst\":\"FAKE_TSA_TOKEN_BASE64\"," +
                "\"rRefs\":{\"ocspRefs\":[{\"digestAlg\":\"%s\"," +
                "\"digestValue\":\"%s\"}]}}",
                fakeDigestAlg, fakeOcspDigest)
                : String.format(
                "{\"rRefs\":{\"ocspRefs\":[{\"digestAlg\":\"%s\"," +
                "\"digestValue\":\"%s\"}]}}",
                fakeDigestAlg, fakeOcspDigest);
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now  = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new SignatureException(
                    OperationOutcomeCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW,
                    String.format(
                            "referenceTimestamp (%d) fora da janela de tolerância de ±%d segundos " +
                                    "em relação ao servidor (%d). Diferença: %d segundos.",
                            referenceTimestamp, TIMESTAMP_TOLERANCE_SECONDS, now, diff));
        }
    }

    private JsonNode decodeAndParseJws(String signatureData) {
        byte[] jwsBytes;
        try {
            jwsBytes = Base64.getDecoder().decode(signatureData);
        } catch (IllegalArgumentException e) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_BASE64_INVALID,
                    "signatureData não é base64 padrão válido (RFC 4648): " + e.getMessage());
        }
        try {
            return objectMapper.readTree(jwsBytes);
        } catch (Exception e) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "Conteúdo decodificado de signatureData não é JSON válido: " + e.getMessage());
        }
    }

    private void validateJwsStructure(JsonNode jws) {
        if (!jws.has("payload") || jws.get("payload").isNull()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS: propriedade 'payload' ausente ou nula.");
        }

        JsonNode signatures = jws.get("signatures");
        if (signatures == null || !signatures.isArray() || signatures.isEmpty()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS: propriedade 'signatures' ausente, não é array ou está vazia.");
        }

        JsonNode sig0 = signatures.get(0);
        if (!sig0.has(PROTECTED) || sig0.get(PROTECTED).isNull()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS: signatures[0].protected ausente ou nulo.");
        }
        if (!sig0.has("signature") || sig0.get("signature").isNull()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS: signatures[0].signature ausente ou nulo.");
        }

        JsonNode protectedHeader = decodeProtectedHeader(sig0.get(PROTECTED).asText());
        validateProtectedHeader(protectedHeader);
    }

    private void validateProtectedHeader(JsonNode protectedHeader) {
        String alg = protectedHeader.path("alg").asText(null);
        if (alg == null) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS protected header: campo 'alg' ausente.");
        }
        if (!"RS256".equals(alg) && !"ES256".equals(alg)) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS protected header: algoritmo '" + alg +
                            "' não suportado. Aceitos: RS256, ES256.");
        }

        JsonNode x5c = protectedHeader.get("x5c");
        if (x5c == null || !x5c.isArray() || x5c.isEmpty()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS protected header: campo 'x5c' ausente ou vazio.");
        }

        JsonNode sigPId = protectedHeader.get("sigPId");
        if (sigPId == null || !sigPId.has("id") || sigPId.get("id").isNull()) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "JWS protected header: campo 'sigPId.id' ausente ou nulo.");
        }
    }

    private JsonNode decodeProtectedHeader(String protectedB64Url) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(protectedB64Url);
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "Não foi possível decodificar o protected header base64Url: " +
                            e.getMessage());
        }
    }

    private void validateIntegrityCheck(VerifyRequestDTO request) {
        Map<String, ?> entryByFullUrl = request.getBundle().getEntry().stream()
                .collect(Collectors.toMap(ResourceEntryDTO::getFullUrl, e -> e));

        for (String ref : request.getProvenance().getTarget()) {
            if (!entryByFullUrl.containsKey(ref)) {
                throw new SignatureException(
                        OperationOutcomeCode.FORMAT_TARGET_REFERENCE_MISSING,
                        "Verificação de integridade: provenance.target referencia '" + ref +
                                "', mas nenhuma entry no bundle possui esse fullUrl.");
            }
        }

        log.info("[FAKE] Verificação de integridade OK ({} targets, {} entries)",
                request.getProvenance().getTarget().size(),
                request.getBundle().getEntry().size());
    }

    private String toBase64Url(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeToBase64Standard(String input) {
        return Base64.getEncoder()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception e) {
            throw new SignatureException(
                    OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                    "Falha ao calcular SHA-256: " + e.getMessage());
        }
    }
}