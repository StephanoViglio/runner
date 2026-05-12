package com.runner.assinador.service.impl;

import com.runner.assinador.dto.outcome.OperationOutcomeDTO;
import com.runner.assinador.dto.request.ResourceEntryDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.*;
import com.runner.assinador.exception.SignatureException;
import com.runner.assinador.factory.OperationOutcomeFactory;
import com.runner.assinador.service.SignatureService;
import com.runner.assinador.utils.OperationOutcomeCode;
import com.runner.assinador.utils.TimestampStrategy;
import com.runner.assinador.validation.SignRequestValidator;
import com.runner.assinador.validation.VerifyRequestValidator;
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

    private static final String FAKE_CERT_DER_BASE64  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAfake";
    private static final String FAKE_CPF              = "98765432100";
    private static final String SIG_TYPE_SYSTEM       = "urn:iso-astm:E1762-95:2013";
    private static final String SIG_TYPE_CODE_AUTORIA = "1.2.840.10065.1.12.1.1";
    private static final String WHO_SYSTEM_CPF        = "urn:brasil:cpf";
    private static final String TARGET_FORMAT         = "application/octet-stream";
    private static final String SIG_FORMAT            = "application/jose";

    private final SignRequestValidator signRequestValidator;
    private final VerifyRequestValidator verifyRequestValidator;

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
    public OperationOutcomeDTO verify(VerifyRequestDTO request) {
        log.info("[FAKE] verify() — iniciando validação das entradas e estrutura JWS");

        verifyRequestValidator.validate(request);

        log.info("[FAKE] JWS válido — retornando VALIDATION.SUCCESS");

        return OperationOutcomeFactory.of(
                OperationOutcomeCode.VALIDATION_SUCCESS,
                "Assinatura verificada com sucesso no modo simulado.");
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