package com.runner.assinador.service.impl;

import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;
import com.runner.assinador.dto.response.VerifyResponseDTO;
import com.runner.assinador.service.SignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class FakeSignatureService implements SignatureService {

    private static final String FAKE_JWS = """
        {
          "payload": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
          "signatures": [{
            "protected": "eyJhbGciOiJSUzI1NiIsInNpZ1BJZCI6eyJpZCI6Imh0dHBzOi8vZmhpci5zYXVkZS5nby5nb3YuYnIvcjQvc2VndXJhbmNhL0ltcGxlbWVudGF0aW9uR3VpZGUvYnIuZ28uc2VzLnNlZ3VyYW5jYXwwLjAuMiJ9fQ",
            "header": { "rRefs": { "ocspRefs": [], "crlRefs": [] } },
            "signature": "FAKE_PKCS11_SIGNATURE_BASE64URL"
          }]
        }
        """;

    @Override
    public SignResponseDTO sign(SignRequestDTO request) {
        log.info("[FAKE] Skipping real PKCS#11 call — returning pre-built JWS");

        SignResponseDTO response = new SignResponseDTO();
        response.setSignatureJson(buildFhirSignature());
        response.setAlgorithm("RS256");
        response.setStrategyUsed(request.getTimestampStrategy() != null
            ? request.getTimestampStrategy() : "iat");
        response.setSigningTimestamp(request.getReferenceTimestamp() != null
            ? request.getReferenceTimestamp()
            : Instant.now().getEpochSecond());

        return response;
    }

    @Override
    public VerifyResponseDTO verify(VerifyRequestDTO request) {
        log.info("[FAKE] verify() called — returning pre-determined result (valid=true)");

        VerifyResponseDTO response = new VerifyResponseDTO();
        response.setValid(true);
        response.setAlgorithm("RS256");
        response.setPolicyUri(
            "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.2");
        response.setSigningTimestamp(request.getReferenceTimestamp() != null
            ? request.getReferenceTimestamp()
            : Instant.now().getEpochSecond());

        return response;
    }

    private String buildFhirSignature() {
        String data = Base64.getEncoder().encodeToString(FAKE_JWS.getBytes());
        return """
            {
              "resourceType": "Signature",
              "type": [{"system": "urn:iso-astm:E1762-95:2013", "code": "1.2.840.10065.1.12.1.1"}],
              "sigFormat": "application/jose",
              "targetFormat": "application/octet-stream",
              "who": {"identifier": {"system": "urn:brasil:cpf", "value": "00000000000"}},
              "data": "%s"
            }
            """.formatted(data);
    }
}