package com.runner.assinador.presentation.out.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.out.SignatureProvider;
import com.runner.assinador.presentation.shared.signature.JwsEnvelope;
import com.runner.assinador.presentation.shared.signature.JwsEnvelopeParser;
import com.runner.assinador.presentation.shared.signature.SignedContentDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

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

        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, request.getSignatureData());
        JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader());

        if (request.hasIntegrityCheck()) {
            validateIntegrity(request.getBundle(), request.getProvenance(), envelope.payload());
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
        return SignedContentDigest.computeBase64Url(request.getBundle(), request.getProvenance());
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

    private void validateIntegrity(BundleData bundle, ProvenanceData provenance, String jwsPayload) {
        log.debug("Executando verificação de integridade do conteúdo assinado");

        String computedHash = SignedContentDigest.computeBase64Url(bundle, provenance);

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
}