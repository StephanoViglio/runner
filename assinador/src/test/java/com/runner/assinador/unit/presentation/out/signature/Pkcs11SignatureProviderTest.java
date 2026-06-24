package com.runner.assinador.unit.presentation.out.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.CryptographicMaterial;
import com.runner.assinador.domain.model.CryptographicStrategy;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.presentation.out.signature.Pkcs11SignatureProvider;
import com.runner.assinador.utils.Pkcs11TestSupport;
import com.runner.assinador.utils.Pkcs11TestSupport.KeyMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static com.runner.assinador.utils.EntityUtils.timestampValido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Pkcs11SignatureProviderTest {

    private final Pkcs11SignatureProvider provider =
            new Pkcs11SignatureProvider(new ObjectMapper(), null);

    @Test
    @DisplayName("buildSignatureResult deve produzir SignatureResult não nulo com chave RSA")
    void buildSignatureResult_deveProduzirResultado_comChaveRsa() {
        KeyMaterial keyMaterial = Pkcs11TestSupport.generateRsaKeyMaterial("Teste RSA");
        SignatureRequest request = signatureRequestCom(keyMaterial);

        SignatureResult result = provider.buildSignatureResult(request, privateKeyEntry(keyMaterial));

        assertThat(result).isNotNull();
        assertThat(result.getSigFormat()).isEqualTo("application/jose");
        assertThat(result.getTargetFormat()).isEqualTo("application/octet-stream");
        assertThat(result.getData()).isNotBlank();
    }

    @Test
    @DisplayName("buildSignatureResult deve extrair o CN do certificado como identidade do signatário")
    void buildSignatureResult_deveExtrairCnDoCertificado() {
        KeyMaterial keyMaterial = Pkcs11TestSupport.generateRsaKeyMaterial("Maria da Silva");
        SignatureRequest request = signatureRequestCom(keyMaterial);

        SignatureResult result = provider.buildSignatureResult(request, privateKeyEntry(keyMaterial));

        assertThat(result.getSignerCpf()).isEqualTo("Maria da Silva");
    }

    @Test
    @DisplayName("buildSignatureResult deve produzir uma assinatura que passa na verificação criptográfica real")
    void buildSignatureResult_deveProduzirAssinaturaValida_naVerificacao() {
        KeyMaterial keyMaterial = Pkcs11TestSupport.generateRsaKeyMaterial("Teste RSA");
        SignatureRequest signRequest = signatureRequestCom(keyMaterial);

        SignatureResult signResult = provider.buildSignatureResult(signRequest, privateKeyEntry(keyMaterial));

        VerificationRequest verifyRequest = new VerificationRequest(
                signResult.getData(), signRequest.getReferenceTimestamp(), POLICY_URI,
                signRequest.getBundle(), signRequest.getProvenance());

        VerificationResult verifyResult = provider.verify(verifyRequest);

        assertThat(verifyResult.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("buildSignatureResult deve funcionar também com chave EC (ES256)")
    void buildSignatureResult_deveFuncionar_comChaveEc() {
        KeyMaterial keyMaterial = Pkcs11TestSupport.generateEcKeyMaterial("Teste EC");
        SignatureRequest signRequest = signatureRequestCom(keyMaterial);

        SignatureResult signResult = provider.buildSignatureResult(signRequest, privateKeyEntry(keyMaterial));

        VerificationRequest verifyRequest = new VerificationRequest(
                signResult.getData(), signRequest.getReferenceTimestamp(), POLICY_URI,
                signRequest.getBundle(), signRequest.getProvenance());

        assertThat(provider.verify(verifyRequest).isSuccess()).isTrue();
    }

    @Test
    @DisplayName("verify deve lançar CRYPTO_SIGNATURE_INVALID quando a assinatura não corresponde ao certificado")
    void verify_deveLancarExcecao_quandoAssinaturaNaoCorrespondeAoCertificado() {
        KeyMaterial assinante = Pkcs11TestSupport.generateRsaKeyMaterial("Assinante");
        KeyMaterial outraChave = Pkcs11TestSupport.generateRsaKeyMaterial("Outra Chave");

        SignatureRequest signRequest = signatureRequestCom(assinante);
        SignatureResult signResult = provider.buildSignatureResult(signRequest, privateKeyEntry(outraChave));

        VerificationRequest verifyRequest = new VerificationRequest(
                signResult.getData(), signRequest.getReferenceTimestamp(), POLICY_URI,
                signRequest.getBundle(), signRequest.getProvenance());

        assertThatThrownBy(() -> provider.verify(verifyRequest))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.CRYPTO_SIGNATURE_INVALID);
    }

    @Test
    @DisplayName("verify deve lançar CRYPTO_SIGNATURE_INVALID quando o payload foi adulterado após a assinatura")
    void verify_deveLancarExcecao_quandoPayloadAdulterado() {
        KeyMaterial keyMaterial = Pkcs11TestSupport.generateRsaKeyMaterial("Teste RSA");
        SignatureRequest signRequest = signatureRequestCom(keyMaterial);
        SignatureResult signResult = provider.buildSignatureResult(signRequest, privateKeyEntry(keyMaterial));

        String jwsAdulterado = new String(Base64.getDecoder().decode(signResult.getData()))
                .replaceFirst("\"payload\":\"[^\"]+\"", "\"payload\":\"adulterado\"");
        String signatureDataAdulterado = Base64.getEncoder().encodeToString(jwsAdulterado.getBytes());

        VerificationRequest verifyRequest = new VerificationRequest(
                signatureDataAdulterado, signRequest.getReferenceTimestamp(), POLICY_URI, null, null);

        assertThatThrownBy(() -> provider.verify(verifyRequest))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.CRYPTO_SIGNATURE_INVALID);
    }

    @Test
    @DisplayName("loadPrivateKeyEntry deve lançar CRYPTO_DEVICE_UNAVAILABLE quando o provider não suporta KeyStore PKCS#11")
    void loadPrivateKeyEntry_deveLancarExcecao_quandoProviderNaoSuportaPkcs11() {
        Pkcs11SignatureProvider providerSemPkcs11 =
                new Pkcs11SignatureProvider(new ObjectMapper(), Security.getProvider("SUN"));
        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "minha-chave", 0, "TOKEN", List.of("cert"));

        assertThatThrownBy(() -> providerSemPkcs11.loadPrivateKeyEntry(material))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.CRYPTO_DEVICE_UNAVAILABLE);
    }

    private SignatureRequest signatureRequestCom(KeyMaterial keyMaterial) {
        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "minha-chave", 0, "TOKEN",
                List.of(keyMaterial.certificateBase64Der()));

        return new SignatureRequest(
                bundleDataValido(), provenanceDataValida(), material,
                timestampValido(), TimestampStrategy.IAT, POLICY_URI);
    }

    private KeyStore.PrivateKeyEntry privateKeyEntry(KeyMaterial keyMaterial) {
        PrivateKey privateKey = keyMaterial.privateKey();
        X509Certificate[] chain = { keyMaterial.certificate() };
        return new KeyStore.PrivateKeyEntry(privateKey, chain);
    }
}
