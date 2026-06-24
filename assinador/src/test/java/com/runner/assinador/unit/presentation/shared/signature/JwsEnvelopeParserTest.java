package com.runner.assinador.unit.presentation.shared.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.presentation.shared.signature.JwsEnvelope;
import com.runner.assinador.presentation.shared.signature.JwsEnvelopeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.utils.SignatureDataUtils.base64InvalidoNaoDecodificavel;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComAlgNaoSuportado;
import static com.runner.assinador.utils.SignatureDataUtils.signatureDataComJsonMalformado;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwsEnvelopeParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("parse deve extrair payload, protected header e signature de um envelope válido")
    void parse_deveExtrairCamposDoEnvelope_quandoValido() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataValido());

        assertThat(envelope.payload()).isEqualTo("qualquer-payload");
        assertThat(envelope.protectedHeader().get("alg").asText()).isEqualTo("RS256");
        assertThat(envelope.signatureB64Url()).isEqualTo("sig");
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_BASE64_INVALID quando signatureData não é base64")
    void parse_deveLancarExcecao_quandoNaoEhBase64() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, base64InvalidoNaoDecodificavel()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_BASE64_INVALID);
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_JSON_MALFORMED quando conteúdo decodificado não é JSON")
    void parse_deveLancarExcecao_quandoJsonMalformado() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, signatureDataComJsonMalformado()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JSON_MALFORMED);
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_JWS_MALFORMED quando payload está ausente")
    void parse_deveLancarExcecao_quandoSemPayload() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, signatureDataSemPayload()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_JWS_MALFORMED quando signatures está ausente")
    void parse_deveLancarExcecao_quandoSemSignaturesArray() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, signatureDataSemSignaturesArray()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_JWS_MALFORMED quando signatures está vazio")
    void parse_deveLancarExcecao_quandoSignaturesVazio() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, signatureDataComSignaturesArrayVazio()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("parse deve lançar FORMAT_JWS_MALFORMED quando signature está ausente")
    void parse_deveLancarExcecao_quandoSemSignatureField() {
        assertThatThrownBy(() -> JwsEnvelopeParser.parse(objectMapper, signatureDataSemSignatureField()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando alg está ausente")
    void validateProtectedHeader_deveLancarExcecao_quandoSemAlg() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComProtectedHeaderSemAlg());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando alg não é suportado")
    void validateProtectedHeader_deveLancarExcecao_quandoAlgNaoSuportado() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComAlgNaoSuportado());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando x5c está ausente")
    void validateProtectedHeader_deveLancarExcecao_quandoSemX5c() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComProtectedHeaderSemX5c());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando x5c está vazio")
    void validateProtectedHeader_deveLancarExcecao_quandoX5cVazio() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComProtectedHeaderX5cVazio());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando sigPId está ausente")
    void validateProtectedHeader_deveLancarExcecao_quandoSemSigPId() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComProtectedHeaderSemSigPId());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader deve lançar FORMAT_JWS_MALFORMED quando sigPId.id está ausente")
    void validateProtectedHeader_deveLancarExcecao_quandoSemSigPIdId() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataComProtectedHeaderSemSigPIdId());

        assertThatThrownBy(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode() == DomainErrorCode.FORMAT_JWS_MALFORMED);
    }

    @Test
    @DisplayName("validateProtectedHeader não deve lançar exceção quando header é válido")
    void validateProtectedHeader_naoDeveLancarExcecao_quandoValido() {
        JwsEnvelope envelope = JwsEnvelopeParser.parse(objectMapper, signatureDataValido());

        assertThatCode(() -> JwsEnvelopeParser.validateProtectedHeader(envelope.protectedHeader()))
                .doesNotThrowAnyException();
    }
}
