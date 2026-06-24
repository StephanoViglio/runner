package com.runner.assinador.presentation.out.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.CryptographicMaterial;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.out.SignatureProvider;
import com.runner.assinador.presentation.shared.signature.JwsEnvelope;
import com.runner.assinador.presentation.shared.signature.JwsEnvelopeParser;
import com.runner.assinador.presentation.shared.signature.SignedContentDigest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AuthProvider;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "assinador.signature-provider", name = "type", havingValue = "pkcs11")
public class Pkcs11SignatureProvider implements SignatureProvider {

    private static final String SIG_TYPE_SYSTEM       = "urn:iso-astm:E1762-95:2013";
    private static final String SIG_TYPE_CODE_AUTORIA  = "1.2.840.10065.1.12.1.1";
    private static final String TARGET_FORMAT          = "application/octet-stream";
    private static final String SIG_FORMAT             = "application/jose";
    private static final String KEYSTORE_TYPE          = "PKCS11";
    private static final String ALG_RS256              = "RS256";
    private static final String ALG_ES256              = "ES256";

    private final ObjectMapper objectMapper;
    private final Provider pkcs11Provider;

    public Pkcs11SignatureProvider(ObjectMapper objectMapper, Provider pkcs11Provider) {
        this.objectMapper = objectMapper;
        this.pkcs11Provider = pkcs11Provider;
    }

    @Override
    public SignatureResult sign(SignatureRequest request) {
        log.info("[PKCS11] sign() — abrindo sessão no dispositivo criptográfico. strategy={}",
                request.getTimestampStrategy());

        CryptographicMaterial material = request.getCryptographicMaterial();
        KeyStore.PrivateKeyEntry keyEntry = loadPrivateKeyEntry(material);

        log.info("[PKCS11] sign() — chave privada obtida, assinando conteúdo");

        return buildSignatureResult(request, keyEntry);
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        log.info("[PKCS11] verify() — validando estrutura e assinatura criptográfica do JWS");

        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, request.getSignatureData());
        JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader());
        verifyCryptographicSignature(envelope);

        if (request.hasIntegrityCheck()) {
            validateIntegrity(envelope.payload(), request);
        }

        log.info("[PKCS11] verify() — assinatura criptográfica válida");

        return VerificationResult.success(
                "Assinatura verificada com sucesso (validação criptográfica real via PKCS#11).");
    }

    public SignatureResult buildSignatureResult(SignatureRequest request, KeyStore.PrivateKeyEntry keyEntry) {
        PrivateKey privateKey = keyEntry.getPrivateKey();
        String alg = jwsAlgorithmFor(privateKey);

        String payload         = SignedContentDigest.computeBase64Url(request.getBundle(), request.getProvenance());
        String protectedHeader = buildProtectedHeader(request, alg);
        String unprotectedHeader = buildUnprotectedHeader(request.getTimestampStrategy());
        String signature        = signDetached(protectedHeader, payload, privateKey, alg);

        String jwsJson        = buildJwsJson(payload, protectedHeader, unprotectedHeader, signature);
        String signatureData  = Base64.getEncoder().encodeToString(jwsJson.getBytes(StandardCharsets.UTF_8));
        String when           = Instant.ofEpochSecond(request.getReferenceTimestamp()).toString();
        String signerIdentity = extractSignerIdentity(
                request.getCryptographicMaterial().getCertificateChain().get(0),
                request.getCryptographicMaterial().getIdentifier());

        return new SignatureResult(
                List.of(new SignatureResult.SignatureCoding(SIG_TYPE_SYSTEM, SIG_TYPE_CODE_AUTORIA)),
                when,
                signerIdentity,
                TARGET_FORMAT,
                SIG_FORMAT,
                signatureData
        );
    }

    public KeyStore.PrivateKeyEntry loadPrivateKeyEntry(CryptographicMaterial material) {
        logoutPreviousSession();

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_TYPE, pkcs11Provider);
            keyStore.load(null, material.getPin().toCharArray());
        } catch (IOException e) {
            if (isLoginFailure(e)) {
                throw new SignatureException(
                        DomainErrorCode.CRYPTO_LOGIN_FAILED,
                        "Falha ao autenticar no dispositivo criptográfico: PIN inválido.");
            }
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_DEVICE_UNAVAILABLE,
                    "Não foi possível acessar o dispositivo criptográfico (slot=" +
                            material.getSlotId() + "): " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_DEVICE_UNAVAILABLE,
                    "Falha ao inicializar o KeyStore PKCS#11: " + e.getMessage());
        }

        try {
            KeyStore.PasswordProtection protection =
                    new KeyStore.PasswordProtection(material.getPin().toCharArray());
            KeyStore.Entry entry = keyStore.getEntry(material.getIdentifier(), protection);

            if (!(entry instanceof KeyStore.PrivateKeyEntry privateKeyEntry)) {
                throw new SignatureException(
                        DomainErrorCode.CRYPTO_KEY_NOT_FOUND,
                        "Nenhuma chave privada encontrada para o identificador '" +
                                material.getIdentifier() + "' no dispositivo (slot=" +
                                material.getSlotId() + ").");
            }
            return privateKeyEntry;
        } catch (GeneralSecurityException e) {
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_KEY_NOT_FOUND,
                    "Falha ao recuperar a chave privada '" + material.getIdentifier() + "': " + e.getMessage());
        }
    }

    private void logoutPreviousSession() {
        if (pkcs11Provider instanceof AuthProvider authProvider) {
            try {
                authProvider.logout();
            } catch (javax.security.auth.login.LoginException e) {
                log.debug("[PKCS11] logout() anterior falhou (ignorado): {}", e.getMessage());
            }
        }
    }

    private boolean isLoginFailure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof javax.security.auth.login.FailedLoginException) {
                return true;
            }
        }
        return false;
    }

    private String buildProtectedHeader(SignatureRequest request, String alg) {
        String x5cJson = request.getCryptographicMaterial().getCertificateChain().stream()
                .map(cert -> "\"" + cert + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        String headerJson = request.getTimestampStrategy() == TimestampStrategy.IAT
                ? String.format(
                "{\"alg\":\"%s\",\"x5c\":%s,\"iat\":%d,\"sigPId\":{\"id\":\"%s\"}}",
                alg, x5cJson, request.getReferenceTimestamp(), request.getPolicyUri())
                : String.format(
                "{\"alg\":\"%s\",\"x5c\":%s,\"sigPId\":{\"id\":\"%s\"}}",
                alg, x5cJson, request.getPolicyUri());

        return toBase64Url(headerJson);
    }

    private String buildUnprotectedHeader(TimestampStrategy strategy) {
        String simulatedDigestAlg  = "http://www.w3.org/2001/04/xmlenc#sha512";
        String simulatedOcspDigest = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        return strategy == TimestampStrategy.TSA
                ? String.format(
                "{\"sigTst\":\"SIMULATED_TSA_TOKEN_BASE64\"," +
                        "\"rRefs\":{\"ocspRefs\":[{\"digestAlg\":\"%s\",\"digestValue\":\"%s\"}]}}",
                simulatedDigestAlg, simulatedOcspDigest)
                : String.format(
                "{\"rRefs\":{\"ocspRefs\":[{\"digestAlg\":\"%s\",\"digestValue\":\"%s\"}]}}",
                simulatedDigestAlg, simulatedOcspDigest);
    }

    private String buildJwsJson(String payload, String protectedHeaderB64Url,
                                 String unprotectedHeaderJson, String signatureB64Url) {
        return String.format(
                "{\"payload\":\"%s\",\"signatures\":[{\"protected\":\"%s\",\"header\":%s,\"signature\":\"%s\"}]}",
                payload, protectedHeaderB64Url, unprotectedHeaderJson, signatureB64Url);
    }

    String signDetached(String protectedHeaderB64Url, String payloadB64Url, PrivateKey privateKey, String alg) {
        String signingInput = protectedHeaderB64Url + "." + payloadB64Url;
        try {
            Signature engine = Signature.getInstance(jcaSignatureAlgorithm(alg));
            engine.initSign(privateKey);
            engine.update(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(engine.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | java.security.SignatureException e) {
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_SIGNING_FAILURE,
                    "Falha ao assinar com o dispositivo criptográfico: " + e.getMessage());
        }
    }

    private void verifyCryptographicSignature(JwsEnvelope envelope) {
        JsonNode x5c = envelope.protectedHeader().get("x5c");
        X509Certificate leafCertificate = decodeCertificate(x5c.get(0).asText());
        String alg = envelope.protectedHeader().get("alg").asText();
        String signingInput = envelope.protectedHeaderB64Url() + "." + envelope.payload();

        try {
            Signature verifier = Signature.getInstance(jcaSignatureAlgorithm(alg));
            verifier.initVerify(leafCertificate.getPublicKey());
            verifier.update(signingInput.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getUrlDecoder().decode(envelope.signatureB64Url());
            if (!verifier.verify(signatureBytes)) {
                throw new SignatureException(
                        DomainErrorCode.CRYPTO_SIGNATURE_INVALID,
                        "Assinatura criptográfica não corresponde ao certificado informado em 'x5c[0]'.");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | java.security.SignatureException
                 | IllegalArgumentException e) {
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_SIGNATURE_INVALID,
                    "Falha ao verificar a assinatura criptográfica: " + e.getMessage());
        }
    }

    private void validateIntegrity(String jwsPayload, VerificationRequest request) {
        log.debug("Executando verificação de integridade do conteúdo assinado");

        String computedHash = SignedContentDigest.computeBase64Url(request.getBundle(), request.getProvenance());
        if (!computedHash.equals(jwsPayload)) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "Verificação de integridade falhou: o hash recalculado do conteúdo " +
                            "não corresponde ao payload do JWS.");
        }
    }

    private X509Certificate decodeCertificate(String base64Der) {
        try {
            byte[] der = Base64.getDecoder().decode(base64Der);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
        } catch (IllegalArgumentException | CertificateException e) {
            throw new SignatureException(
                    DomainErrorCode.FORMAT_JWS_MALFORMED,
                    "JWS protected header: certificado em 'x5c[0]' não é um certificado X.509 válido: "
                            + e.getMessage());
        }
    }

    private String extractSignerIdentity(String leafCertificateBase64Der, String fallbackIdentifier) {
        try {
            X509Certificate cert = decodeCertificate(leafCertificateBase64Der);
            for (String rdn : cert.getSubjectX500Principal().getName().split(",")) {
                String trimmed = rdn.trim();
                if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
                    return trimmed.substring(3);
                }
            }
            return fallbackIdentifier;
        } catch (Exception e) {
            return fallbackIdentifier;
        }
    }

    private String jwsAlgorithmFor(PrivateKey privateKey) {
        return switch (privateKey.getAlgorithm()) {
            case "RSA" -> ALG_RS256;
            case "EC" -> ALG_ES256;
            default -> throw new SignatureException(
                    DomainErrorCode.CRYPTO_SIGNING_FAILURE,
                    "Tipo de chave não suportado pelo dispositivo: " + privateKey.getAlgorithm());
        };
    }

    private String jcaSignatureAlgorithm(String jwsAlg) {
        return switch (jwsAlg) {
            case ALG_RS256 -> "SHA256withRSA";
            case ALG_ES256 -> "SHA256withECDSA";
            default -> throw new SignatureException(
                    DomainErrorCode.CRYPTO_SIGNATURE_INVALID,
                    "Algoritmo '" + jwsAlg + "' não suportado para verificação criptográfica.");
        };
    }

    private String toBase64Url(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}
