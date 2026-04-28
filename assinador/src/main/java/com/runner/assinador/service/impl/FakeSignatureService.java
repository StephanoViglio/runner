package com.runner.assinador.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.dto.request.ResourceEntryDTO;
import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;
import com.runner.assinador.dto.response.VerifyResponseDTO;
import com.runner.assinador.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakeSignatureService implements SignatureService {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;

    private static final String POLICY_BASE_URI =
            "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|";

    private static final String SUPPORTED_POLICY_VERSION = "0.0.2";

    private static final String FAKE_ALGORITHM = "RS256";

    private final ObjectMapper objectMapper;

    @Override
    public SignResponseDTO sign(SignRequestDTO request) {
        log.info("[FAKE] sign() — validando entradas");

        validatePolicy(request.getPolicyUri());
        validateTimestampWindow(request.getReferenceTimestamp());
        validateBundleEntries(request);
        validateProvenanceTargets(request);

        log.info("[FAKE] Entradas válidas — retornando SignResponseDTO. strategy={}", request.getTimestampStrategy());

        SignResponseDTO response = new SignResponseDTO();
        response.setSignatureJson(buildFakeJwsJson(request));
        response.setAlgorithm(FAKE_ALGORITHM);
        response.setStrategyUsed(request.getTimestampStrategy());
        response.setSigningTimestamp(request.getReferenceTimestamp());
        return response;
    }

    @Override
    public VerifyResponseDTO verify(VerifyRequestDTO request) {
        log.info("[FAKE] verify() — validando entradas e estrutura JWS");

        validatePolicy(request.getPolicyUri());
        validateTimestampWindow(request.getReferenceTimestamp());

        JsonNode jws = decodeAndParseJws(request.getSignatureData());
        validateJwsStructure(jws);

        if (request.getBundle() != null && request.getProvenance() != null) {
            log.info("[FAKE] Bundle e Provenance fornecidos — verificação de integridade simulada");
            validateIntegrityCheck(request);
        } else if (request.getBundle() != null || request.getProvenance() != null) {
            log.warn("[FAKE] bundle e provenance devem ser fornecidos em conjunto para verificação de integridade");
        }

        log.info("[FAKE] JWS válido — retornando VerifyResponseDTO valid=true");

        VerifyResponseDTO response = new VerifyResponseDTO();
        response.setValid(true);
        response.setAlgorithm(FAKE_ALGORITHM);
        response.setPolicyUri(request.getPolicyUri());
        response.setSigningTimestamp(request.getReferenceTimestamp());
        return response;
    }

    private void validatePolicy(String policyUri) {
        if (!policyUri.startsWith(POLICY_BASE_URI)) {
            throw new RuntimeException(
                    "URI da política inválida. Deve iniciar com: " + POLICY_BASE_URI);
        }
        String version = policyUri.substring(POLICY_BASE_URI.length());
        if (!SUPPORTED_POLICY_VERSION.equals(version)) {
            throw new RuntimeException(String.format(
                    "Versão da política '%s' não suportada. Versões suportadas: ['%s'].",
                    version, SUPPORTED_POLICY_VERSION));
        }
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new RuntimeException(String.format(
                    "Timestamp de referência (%d) fora da janela de tolerância de ±%d segundos " +
                            "em relação ao servidor (%d). Diferença: %d segundos.",
                    referenceTimestamp, TIMESTAMP_TOLERANCE_SECONDS, now, diff));
        }
    }

    private void validateBundleEntries(SignRequestDTO request) {
        Set<String> seen = new HashSet<>();
        for (ResourceEntryDTO entry : request.getBundle().getEntry()) {
            if (!seen.add(entry.getFullUrl())) {
                throw new RuntimeException(
                        "bundle.entry contém fullUrl duplicado: '" + entry.getFullUrl() + "'.");
            }
        }
    }

    private void validateProvenanceTargets(SignRequestDTO request) {
        Map<String, ResourceEntryDTO> entryByFullUrl = request.getBundle().getEntry().stream()
                .collect(Collectors.toMap(ResourceEntryDTO::getFullUrl, e -> e));

        Set<String> seen = new HashSet<>();
        for (String ref : request.getProvenance().getTarget()) {

            if (!seen.add(ref)) {
                throw new RuntimeException(
                        "provenance.target contém referência duplicada: '" + ref + "'.");
            }

            ResourceEntryDTO matchingEntry = entryByFullUrl.get(ref);
            if (matchingEntry == null) {
                throw new RuntimeException(
                        "provenance.target referencia '" + ref + "', mas nenhuma entry em " +
                                "bundle.entry possui esse fullUrl.");
            }

            if (matchingEntry.getResourceJson() == null || matchingEntry.getResourceJson().isBlank()) {
                throw new RuntimeException(
                        "A entry com fullUrl '" + ref + "' está referenciada em provenance.target " +
                                "mas não possui resourceJson.");
            }
        }
    }

    private void validateIntegrityCheck(VerifyRequestDTO request) {
        Map<String, ResourceEntryDTO> entryByFullUrl = request.getBundle().getEntry().stream()
                .collect(Collectors.toMap(ResourceEntryDTO::getFullUrl, e -> e));

        for (String ref : request.getProvenance().getTarget()) {
            if (!entryByFullUrl.containsKey(ref)) {
                throw new RuntimeException(
                        "Verificação de integridade: provenance.target referencia '" + ref +
                                "', mas nenhuma entry no bundle possui esse fullUrl.");
            }
        }

        log.info("[FAKE] Verificação de integridade OK ({} targets, {} entries)",
                request.getProvenance().getTarget().size(),
                request.getBundle().getEntry().size());
    }

    private JsonNode decodeAndParseJws(String signatureData) {
        byte[] jwsBytes;
        try {
            jwsBytes = Base64.getDecoder().decode(signatureData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "signatureData não é base64 padrão válido (RFC 4648): " + e.getMessage());
        }
        try {
            return objectMapper.readTree(jwsBytes);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Conteúdo decodificado de signatureData não é JSON válido: " + e.getMessage());
        }
    }

    private void validateJwsStructure(JsonNode jws) {
        if (!jws.has("payload") || jws.get("payload").isNull()) {
            throw new RuntimeException("JWS: propriedade 'payload' ausente ou nula.");
        }

        JsonNode signatures = jws.get("signatures");
        if (signatures == null || !signatures.isArray() || signatures.isEmpty()) {
            throw new RuntimeException("JWS: propriedade 'signatures' ausente, não é array ou está vazia.");
        }

        JsonNode sig0 = signatures.get(0);
        if (!sig0.has("protected") || sig0.get("protected").isNull()) {
            throw new RuntimeException("JWS: signatures[0].protected ausente ou nulo.");
        }
        if (!sig0.has("signature") || sig0.get("signature").isNull()) {
            throw new RuntimeException("JWS: signatures[0].signature ausente ou nulo.");
        }

        JsonNode protectedHeader = decodeProtectedHeader(sig0.get("protected").asText());

        String alg = protectedHeader.path("alg").asText(null);
        if (alg == null) {
            throw new RuntimeException("JWS protected header: campo 'alg' ausente.");
        }
        if (!"RS256".equals(alg) && !"ES256".equals(alg)) {
            throw new RuntimeException(
                    "JWS protected header: algoritmo '" + alg + "' não suportado. Aceitos: RS256, ES256.");
        }

        JsonNode x5c = protectedHeader.get("x5c");
        if (x5c == null || !x5c.isArray() || x5c.isEmpty()) {
            throw new RuntimeException("JWS protected header: campo 'x5c' ausente ou vazio.");
        }

        JsonNode sigPId = protectedHeader.get("sigPId");
        if (sigPId == null || !sigPId.has("id") || sigPId.get("id").isNull()) {
            throw new RuntimeException("JWS protected header: campo 'sigPId.id' ausente ou nulo.");
        }

        validatePolicy(sigPId.get("id").asText());
    }

    private JsonNode decodeProtectedHeader(String protectedB64) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(protectedB64);
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível decodificar o protected header base64Url: " + e.getMessage());
        }
    }

    private String buildFakeJwsJson(SignRequestDTO request) {
        String protectedHeader = toBase64Url(String.format(
                "{\"alg\":\"RS256\",\"x5c\":[\"FAKE_CERT_BASE64\"],\"sigPId\":{\"id\":\"%s\"}}",
                request.getPolicyUri()));

        String payload = toBase64Url("FAKE_SHA256_HASH_OF_CANONICALIZED_BUNDLE_CONTENT");

        String unprotectedHeader = switch (request.getTimestampStrategy()) {
            case IAT -> "{\"rRefs\":{\"ocspRefs\":[],\"crlRefs\":[]}}";
            case TSA -> "{\"sigTst\":\"FAKE_TSA_TOKEN_BASE64\",\"rRefs\":{\"ocspRefs\":[],\"crlRefs\":[]}}";
        };

        return String.format(
                "{\"payload\":\"%s\",\"signatures\":[{\"protected\":\"%s\"," +
                        "\"header\":%s,\"signature\":\"FAKE_PKCS11_SIGNATURE_BASE64URL\"}]}",
                payload, protectedHeader, unprotectedHeader);
    }

    private String toBase64Url(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes());
    }
}