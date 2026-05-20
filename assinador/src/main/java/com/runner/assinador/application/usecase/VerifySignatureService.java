package com.runner.assinador.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.application.command.VerifySignatureCommand;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import com.runner.assinador.domain.port.out.SignatureProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifySignatureService implements VerifySignatureUseCase {

    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300L;
    private static final String PROTECTED = "protected";

    private final SignatureProvider signatureProvider;
    private final ObjectMapper objectMapper;

    @Override
    public VerificationResult execute(VerifySignatureCommand command) {
        log.info("VerifySignatureService.execute() — iniciando validações de negócio");

        validateTimestampWindow(command.getReferenceTimestamp());

        JsonNode jws             = decodeAndParseJws(command.getSignatureData());
        JsonNode sig0            = validateJwsStructure(jws);
        JsonNode protectedHeader = decodeProtectedHeader(sig0.get(PROTECTED).asText());
        validateProtectedHeader(protectedHeader);

        if (command.hasIntegrityCheck()) {
            validateIntegrity(command.getBundle(), command.getProvenance(), jws.get("payload").asText());
        }

        log.info("Validações concluídas — delegando para SignatureProvider");

        return signatureProvider.verify(command);
    }

    private void validateTimestampWindow(Long referenceTimestamp) {
        long now  = Instant.now().getEpochSecond();
        long diff = Math.abs(referenceTimestamp - now);
        if (diff > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new SignatureException(
                    DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW,
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