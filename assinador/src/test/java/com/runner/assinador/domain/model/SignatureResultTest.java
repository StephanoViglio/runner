package com.runner.assinador.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignatureResultTest {

    private static final String SYSTEM = "urn:iso-astm:E1762-95:2013";
    private static final String CODE   = "1.2.840.10065.1.12.1.1";

    @Test
    @DisplayName("Deve construir com sucesso e expor todos os campos")
    void signatureResult_deveConstruirComSucesso_quandoTodosCamposValidos() {
        SignatureResult.SignatureCoding coding = new SignatureResult.SignatureCoding(SYSTEM, CODE);

        SignatureResult result = new SignatureResult(
                List.of(coding),
                "2026-06-02T12:00:00Z",
                "12345678900",
                "application/octet-stream",
                "application/jose",
                "FAKE_SIGNATURE_DATA"
        );

        assertThat(result.getType()).containsExactly(coding);
        assertThat(result.getWhen()).isEqualTo("2026-06-02T12:00:00Z");
        assertThat(result.getSignerCpf()).isEqualTo("12345678900");
        assertThat(result.getTargetFormat()).isEqualTo("application/octet-stream");
        assertThat(result.getSigFormat()).isEqualTo("application/jose");
        assertThat(result.getData()).isEqualTo("FAKE_SIGNATURE_DATA");
    }

    @Test
    @DisplayName("Deve preservar o estado interno quando a lista original de codings é alterada")
    void signatureResult_deveSerImutavel_quandoListaOriginalDeTypeAlterada() {
        List<SignatureResult.SignatureCoding> mutavel = new ArrayList<>();
        mutavel.add(new SignatureResult.SignatureCoding(SYSTEM, CODE));

        SignatureResult result = new SignatureResult(
                mutavel, "2026-06-02T12:00:00Z", "12345678900",
                "application/octet-stream", "application/jose", "DATA"
        );
        mutavel.add(new SignatureResult.SignatureCoding("outro", "outro"));

        assertThat(result.getType()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar uma lista imutável quando getType() é chamado")
    void signatureResult_deveRetornarListaImutavel_quandoChamarGetType() {
        SignatureResult result = new SignatureResult(
                List.of(new SignatureResult.SignatureCoding(SYSTEM, CODE)),
                "2026-06-02T12:00:00Z", "12345678900",
                "application/octet-stream", "application/jose", "DATA"
        );

        assertThatThrownBy(() -> result.getType().add(new SignatureResult.SignatureCoding("x", "y")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("SignatureCoding record deve expor system e code via accessors")
    void signatureCoding_deveExporSystemECode_quandoConstruidoComValores() {
        SignatureResult.SignatureCoding coding = new SignatureResult.SignatureCoding(SYSTEM, CODE);

        assertThat(coding.system()).isEqualTo(SYSTEM);
        assertThat(coding.code()).isEqualTo(CODE);
    }
}