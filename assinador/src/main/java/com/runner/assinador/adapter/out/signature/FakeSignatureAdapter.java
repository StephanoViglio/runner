package com.runner.assinador.adapter.out.signature;

import com.runner.assinador.application.command.SignDocumentCommand;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.out.SignatureProvider;
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
public class FakeSignatureAdapter implements SignatureProvider {

    private static final String FAKE_CERT_DER_BASE64  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAfake";
    private static final String FAKE_CPF              = "98765432100";
    private static final String SIG_TYPE_SYSTEM       = "urn:iso-astm:E1762-95:2013";
    private static final String SIG_TYPE_CODE_AUTORIA = "1.2.840.10065.1.12.1.1";
    private static final String TARGET_FORMAT         = "application/octet-stream";
    private static final String SIG_FORMAT            = "application/jose";

    @Override
    public SignatureResult sign(SignDocumentCommand command) {
        log.info("[FAKE] sign() — construindo JWS. strategy={}", command.getTimestampStrategy());

        String jwsJson       = buildFakeJwsJson(command);
        String signatureData = encodeToBase64Standard(jwsJson);
        String when          = Instant.ofEpochSecond(command.getReferenceTimestamp()).toString();

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
    public VerificationResult verify(VerifySignatureCommand command) {
        log.info("[FAKE] verify() — retornando VALIDATION.SUCCESS");

        return VerificationResult.success("Assinatura verificada com sucesso no modo simulado.");
    }

    private String buildFakeJwsJson(SignDocumentCommand command) {
        String protectedHeader = buildFakeProtectedHeader(command);
        String payload         = buildFakePayload(command);
        String header          = buildFakeUnprotectedHeader(command.getTimestampStrategy());

        return String.format(
                "{\"payload\":\"%s\",\"signatures\":[{\"protected\":\"%s\"," +
                "\"header\":%s,\"signature\":\"%s\"}]}",
                payload,
                protectedHeader,
                header,
                "FAKE_PKCS11_SIGNATURE_BASE64URL");
    }

    private String buildFakeProtectedHeader(SignDocumentCommand command) {
        String headerJson = command.getTimestampStrategy() == TimestampStrategy.IAT
                ? String.format(
                        "{\"alg\":\"RS256\",\"x5c\":[\"%s\"],\"iat\":%d," +
                        "\"sigPId\":{\"id\":\"%s\"}}",
                        FAKE_CERT_DER_BASE64,
                        command.getReferenceTimestamp(),
                        command.getPolicyUri())
                : String.format(
                        "{\"alg\":\"RS256\",\"x5c\":[\"%s\"]," +
                        "\"sigPId\":{\"id\":\"%s\"}}",
                        FAKE_CERT_DER_BASE64,
                        command.getPolicyUri());

        return toBase64Url(headerJson);
    }

    private String buildFakePayload(SignDocumentCommand command) {
        Map<String, ResourceEntry> entryByFullUrl = command.getBundle().getEntries().stream()
                .collect(Collectors.toMap(ResourceEntry::getFullUrl, e -> e));

        StringBuilder concatenated = new StringBuilder();
        for (String ref : command.getProvenance().getTargets()) {
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
                    DomainErrorCode.FORMAT_JSON_MALFORMED,
                    "Falha ao calcular SHA-256: " + e.getMessage());
        }
    }
}