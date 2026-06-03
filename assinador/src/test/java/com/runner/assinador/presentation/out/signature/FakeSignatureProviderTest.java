package com.runner.assinador.presentation.out.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.domain.model.TimestampStrategy.IAT;
import static com.runner.assinador.domain.model.TimestampStrategy.TSA;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.cryptographicMaterialValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static com.runner.assinador.utils.EntityUtils.signatureRequestValido;
import static com.runner.assinador.utils.EntityUtils.verificationRequestComSignatureData;
import static com.runner.assinador.utils.SignatureDataUtils.base64InvalidoNaoDecodificavel;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComAlgNaoSuportado;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComJsonMalformado;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComPayloadAlterado;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderNaoBase64Url;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderSemAlg;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderSemSigPId;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderSemSigPIdId;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderSemX5c;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComProtectedHeaderX5cVazio;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComSignaturesArrayVazio;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataSemPayload;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataSemSignatureField;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataSemSignaturesArray;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataValido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakeSignatureProviderTest {

    private FakeSignatureProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FakeSignatureProvider(new ObjectMapper());
    }

    @Test
    @DisplayName("sign deve retornar um SignatureResult não nulo quando recebe um request válido")
    void sign_deveRetornarSignatureResult_quandoRequestValido() {
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("sign deve retornar SignatureResult com sigFormat application/jose")
    void sign_deveRetornarSigFormatJose_quandoRequestValido() {
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result.getSigFormat()).isEqualTo("application/jose");
    }

    @Test
    @DisplayName("sign deve retornar SignatureResult com targetFormat application/octet-stream")
    void sign_deveRetornarTargetFormatOctetStream_quandoRequestValido() {
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result.getTargetFormat()).isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("sign deve retornar SignatureResult com type contendo coding de autoria E1762-95")
    void sign_deveRetornarTypeComCodingAutoria_quandoRequestValido() {
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result.getType()).hasSize(1);
        assertThat(result.getType().get(0).system()).isEqualTo("urn:iso-astm:E1762-95:2013");
        assertThat(result.getType().get(0).code()).isEqualTo("1.2.840.10065.1.12.1.1");
    }

    @Test
    @DisplayName("sign deve retornar SignatureResult com data em base64 não vazia")
    void sign_deveRetornarDataEmBase64NaoVazia_quandoRequestValido() {
        SignatureRequest request = signatureRequestValido();

        SignatureResult result = provider.sign(request);

        assertThat(result.getData()).isNotBlank();
    }

    @Test
    @DisplayName("sign deve usar o referenceTimestamp do request para compor o campo when")
    void sign_deveUsarReferenceTimestamp_paraComporWhen() {
        long referenceTimestamp = 1751328000L;
        SignatureRequest request = new SignatureRequest(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                referenceTimestamp,
                IAT,
                POLICY_URI
        );

        SignatureResult result = provider.sign(request);

        assertThat(result.getWhen()).isEqualTo("2025-07-01T00:00:00Z");
    }

    @Test
    @DisplayName("sign deve produzir SignatureData diferente quando a strategy é TSA em vez de IAT")
    void sign_deveProduzirSignatureDataDiferente_quandoStrategyTSAvsIAT() {
        SignatureRequest requestIAT = new SignatureRequest(
                bundleDataValido(), provenanceDataValida(), cryptographicMaterialValido(),
                1751328000L, IAT, POLICY_URI);
        SignatureRequest requestTSA = new SignatureRequest(
                bundleDataValido(), provenanceDataValida(), cryptographicMaterialValido(),
                1751328000L, TSA, POLICY_URI);

        SignatureResult resultIAT = provider.sign(requestIAT);
        SignatureResult resultTSA = provider.sign(requestTSA);

        assertThat(resultIAT.getData()).isNotEqualTo(resultTSA.getData());
    }

    @Test
    @DisplayName("verify deve retornar success quando recebe um signatureData produzido pelo próprio sign()")
    void verify_deveRetornarSuccess_quandoSignatureDataProduzidoPeloSign() {
        SignatureRequest signRequest = signatureRequestValido();
        SignatureResult signResult = provider.sign(signRequest);

        VerificationRequest verifyRequest = new VerificationRequest(
                signResult.getData(),
                signRequest.getReferenceTimestamp(),
                POLICY_URI,
                signRequest.getBundle(),
                signRequest.getProvenance()
        );

        VerificationResult result = provider.verify(verifyRequest);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("verify deve retornar success quando não há bundle e provenance para checar integridade")
    void verify_deveRetornarSuccess_quandoSemBundleParaChecarIntegridade() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataValido());

        VerificationResult result = provider.verify(request);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_BASE64_INVALID quando signatureData não é base64 válido")
    void verify_deveLancarExcecao_quandoSignatureDataNaoEhBase64Valido() {
        VerificationRequest request = verificationRequestComSignatureData(base64InvalidoNaoDecodificavel());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_BASE64_INVALID);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JSON_MALFORMED quando conteúdo decodificado não é JSON válido")
    void verify_deveLancarExcecao_quandoConteudoDecodificadoNaoEhJsonValido() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataComJsonMalformado());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JSON_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando JWS não tem payload")
    void verify_deveLancarExcecao_quandoJwsSemPayload() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataSemPayload());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando JWS não tem array signatures")
    void verify_deveLancarExcecao_quandoJwsSemArraySignatures() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataSemSignaturesArray());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando array signatures está vazio")
    void verify_deveLancarExcecao_quandoArraySignaturesVazio() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataComSignaturesArrayVazio());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando primeira signature não tem protected")
    void verify_deveLancarExcecao_quandoPrimeiraSignatureSemProtected() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataSemPayload());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando primeira signature não tem signature")
    void verify_deveLancarExcecao_quandoPrimeiraSignatureSemSignatureField() {
        VerificationRequest request = verificationRequestComSignatureData(signatureDataSemSignatureField());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando protected header não é base64Url decodificável")
    void verify_deveLancarExcecao_quandoProtectedHeaderNaoBase64Url() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderNaoBase64Url());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando protected header não tem alg")
    void verify_deveLancarExcecao_quandoProtectedHeaderSemAlg() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderSemAlg());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando alg não é RS256 nem ES256")
    void verify_deveLancarExcecao_quandoAlgNaoSuportado() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComAlgNaoSuportado());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando protected header não tem x5c")
    void verify_deveLancarExcecao_quandoProtectedHeaderSemX5c() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderSemX5c());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando x5c é array vazio")
    void verify_deveLancarExcecao_quandoX5cVazio() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderX5cVazio());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando protected header não tem sigPId")
    void verify_deveLancarExcecao_quandoProtectedHeaderSemSigPId() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderSemSigPId());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando sigPId não tem campo id")
    void verify_deveLancarExcecao_quandoSigPIdSemId() {
        VerificationRequest request = verificationRequestComSignatureData(
                signatureDataComProtectedHeaderSemSigPIdId());

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("verify deve lançar exceção com código FORMAT_JWS_MALFORMED quando hash recalculado não bate com payload do JWS")
    void verify_deveLancarExcecao_quandoHashRecalculadoNaoBateComPayload() {
        VerificationRequest request = new VerificationRequest(
                signatureDataComPayloadAlterado("payload-que-nao-bate-com-hash-real"),
                1751328000L,
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );

        assertThatThrownBy(() -> provider.verify(request))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }
}