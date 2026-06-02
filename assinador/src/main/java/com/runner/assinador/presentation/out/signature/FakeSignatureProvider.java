package com.runner.assinador.presentation.out.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.out.SignatureProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeSignatureProvider implements SignatureProvider {

    private static final String FAKE_CERT_DER_BASE64  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAfake";
    private static final String FAKE_CPF              = "98765432100";
    private static final String SIG_TYPE_SYSTEM       = "urn:iso-astm:E1762-95:2013";
    private static final String SIG_TYPE_CODE_AUTORIA = "1.2.840.10065.1.12.1.1";
    private static final String TARGET_FORMAT         = "application/octet-stream";
    private static final String SIG_FORMAT            = "application/jose";
    private static final String PROTECTED             = "protected";

    private final ObjectMapper objectMapper;

    @Override
    public SignatureResult sign(SignatureRequest request) {
        log.info("[FAKE] sign() — construindo JWS. strategy={}", request.getTimestampStrategy());

        String jwsJson       = buildFakeJwsJson(request);
        String signatureData = encodeToBase64Standard(jwsJson);
        String when          = Instant.ofEpochSecond(request.getReferenceTimestamp()).toString();

        return new SignatureResult(
                List.of(new SignatureResult.SignatureCoding(SIG_TYPE_SYSTEM, SIG_TYPE_CODE_AUTORIA)),
                when,
                FAKE_CPF,
                TARGET_FORMAT,
                SIG_FORMAT,
                signatureData
        );
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        log.info("[FAKE] verify() — validando estrutura do JWS");

        JsonNode jws             = decodeAndParseJws(request.getSignatureData());
        JsonNode sig0            = validateJwsStructure(jws);
        JsonNode protectedHeader = decodeProtectedHeader(sig0.get(PROTECTED).asText());
        validateProtectedHeader(protectedHeader);

        if (request.hasIntegrityCheck()) {
            validateIntegrity(request.getBundle(), request.getProvenance(), jws.get("payload").asText());
        }

        log.info("[FAKE] verify() — assinatura considerada válida (modo simulado)");

        return VerificationResult.success("Assinatura verificada com sucesso no modo simulado.");
    }

    private String buildFakeJwsJson(SignatureRequest request) {
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

    private String buildFakeProtectedHeader(SignatureRequest request) {
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

    private String buildFakePayload(SignatureRequest request) {
        Map<String, ResourceEntry> entryByFullUrl = request.getBundle().getEntries().stream()
                .collect(Collectors.toMap(ResourceEntry::getFullUrl, e -> e));

        StringBuilder concatenated = new StringBuilder();
        for (String ref : request.getProvenance().getTargets()) {
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

    private JsonNode decodeAndParseJws(String signatureData) {
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

    private JsonNode validateJwsStructure(JsonNode jws) {
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

    private JsonNode decodeProtectedHeader(String protectedB64Url) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(protectedB64Url);
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "Não foi possível decodificar o protected header base64Url: " + e.getMessage());
        }
    }

    private void validateProtectedHeader(JsonNode protectedHeader) {
        String alg = protectedHeader.path("alg").asText(null);
        if (alg == null) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: campo 'alg' ausente.");
        }
        if (!"RS256".equals(alg) && !"ES256".equals(alg)) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: algoritmo '" + alg +
                            "' não suportado. Aceitos: RS256, ES256.");
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

    private void validateIntegrity(BundleData bundle, ProvenanceData provenance, String jwsPayload) {
        log.debug("Executando verificação de integridade do conteúdo assinado");

        Map<String, ResourceEntry> entryByFullUrl = bundle.getEntries().stream()
                .collect(Collectors.toMap(ResourceEntry::getFullUrl, e -> e));

        for (String ref : provenance.getTargets()) {
            if (!entryByFullUrl.containsKey(ref)) {
                throw new SignatureException(
                        DomainErrorCode.FORMAT_TARGET_REFERENCE_MISSING,
                        "Verificação de integridade: provenance.target referencia '" + ref +
                                "', mas nenhuma entry no bundle possui esse fullUrl.");
            }
        }

        StringBuilder concatenated = new StringBuilder();
        for (String ref : provenance.getTargets()) {
            concatenated.append(entryByFullUrl.get(ref).getResourceJson());
        }

        byte[] sha256       = sha256(concatenated.toString().getBytes(StandardCharsets.UTF_8));
        String computedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256);

        if (!computedHash.equals(jwsPayload)) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "Verificação de integridade falhou: o hash recalculado do conteúdo " +
                            "não corresponde ao payload do JWS.");
        }

        log.info("Verificação de integridade OK ({} targets, {} entries)",
                provenance.getTargets().size(), bundle.getEntries().size());
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
                    DomainErrorCode.CRYPTO_DIGEST_FAILURE,
                    "Falha ao calcular SHA-256: " + e.getMessage());
        }
    }
}