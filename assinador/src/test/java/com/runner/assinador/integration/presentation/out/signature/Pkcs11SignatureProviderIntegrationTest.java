package com.runner.assinador.integration.presentation.out.signature;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.List;

import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static com.runner.assinador.utils.EntityUtils.timestampValido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "PKCS11_LIBRARY_PATH", matches = ".+")
class Pkcs11SignatureProviderIntegrationTest {

    private static Provider pkcs11Provider;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void registerProvider() throws IOException {
        String libraryPath = System.getenv("PKCS11_LIBRARY_PATH");
        String slot = System.getenv().getOrDefault("PKCS11_SLOT", "0");

        String config = """
                name = AssinadorIT
                library = %s
                slotListIndex = %s
                """.formatted(libraryPath, slot);

        Path configFile = tempDir.resolve("pkcs11-it.cfg");
        Files.writeString(configFile, config, StandardCharsets.UTF_8);

        Provider template = Security.getProvider("SunPKCS11");
        pkcs11Provider = template.configure(configFile.toString());
        Security.addProvider(pkcs11Provider);
    }

    @AfterAll
    static void removeProvider() {
        if (pkcs11Provider != null) {
            Security.removeProvider(pkcs11Provider.getName());
        }
    }

    @Test
    @DisplayName("sign deve assinar com sucesso usando a chave real armazenada no token SoftHSM2")
    void sign_deveAssinarComSucesso_usandoChaveDoToken() {
        Pkcs11SignatureProvider provider = new Pkcs11SignatureProvider(new ObjectMapper(), pkcs11Provider);
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotBlank();
    }

    @Test
    @DisplayName("sign + verify devem fechar o ciclo completo: assinar no token e validar a assinatura real")
    void signEVerify_devemFecharOCicloCompleto() {
        Pkcs11SignatureProvider provider = new Pkcs11SignatureProvider(new ObjectMapper(), pkcs11Provider);
        SignatureRequest signRequest = signatureRequestValido();

        SignatureResult signResult = provider.sign(signRequest);

        VerificationRequest verifyRequest = new VerificationRequest(
                signResult.getData(), signRequest.getReferenceTimestamp(), POLICY_URI,
                signRequest.getBundle(), signRequest.getProvenance());

        VerificationResult verifyResult = provider.verify(verifyRequest);

        assertThat(verifyResult.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("sign deve lançar CRYPTO_LOGIN_FAILED quando o PIN informado está incorreto")
    void sign_deveLancarExcecao_quandoPinIncorreto() {
        Pkcs11SignatureProvider provider = new Pkcs11SignatureProvider(new ObjectMapper(), pkcs11Provider);
        SignatureRequest request = signatureRequestCom(pinInvalido(), keyAlias());

        assertThatThrownBy(() -> provider.sign(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.CRYPTO_LOGIN_FAILED);
    }

    @Test
    @DisplayName("sign deve lançar CRYPTO_KEY_NOT_FOUND quando o identificador da chave não existe no token")
    void sign_deveLancarExcecao_quandoIdentifierNaoExisteNoToken() {
        Pkcs11SignatureProvider provider = new Pkcs11SignatureProvider(new ObjectMapper(), pkcs11Provider);
        SignatureRequest request = signatureRequestCom(pin(), "alias-que-nao-existe-no-token");

        assertThatThrownBy(() -> provider.sign(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.CRYPTO_KEY_NOT_FOUND);
    }

    private SignatureRequest signatureRequestValido() {
        return signatureRequestCom(pin(), keyAlias());
    }

    private SignatureRequest signatureRequestCom(String pin, String identifier) {
        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.TOKEN, pin, identifier, slotIndex(), "SOFTHSM2",
                List.of(certificateChainBase64Der(keyAlias())));

        return new SignatureRequest(
                bundleDataValido(), provenanceDataValida(), material,
                timestampValido(), TimestampStrategy.IAT, POLICY_URI);
    }

    private String certificateChainBase64Der(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
            keyStore.load(null, pin().toCharArray());
            Certificate certificate = keyStore.getCertificate(alias);
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            throw new UncheckedIOException(
                    new IOException("Falha ao ler certificado do token para o alias '" + alias + "'", e));
        }
    }

    private String pin() {
        return System.getenv().getOrDefault("PKCS11_PIN", "1234");
    }

    private String pinInvalido() {
        return "0000-pin-invalido";
    }

    private String keyAlias() {
        return System.getenv().getOrDefault("PKCS11_KEY_ALIAS", "assinador-it");
    }

    private Integer slotIndex() {
        return Integer.parseInt(System.getenv().getOrDefault("PKCS11_SLOT", "0"));
    }
}
