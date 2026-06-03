package com.runner.assinador.domain.model;

import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationRequestTest {

    private static final String SIGNATURE_DATA = "U0lHTkFUVVJFX0RBVEFfQkFTRTY0";

    @Test
    @DisplayName("Deve construir com sucesso quando todos os campos obrigatórios são válidos e bundle+provenance presentes")
    void verificationRequest_deveConstruirComSucesso_quandoTodosCamposValidos() {
        VerificationRequest request = new VerificationRequest(
                SIGNATURE_DATA,
                1751328000L,
                EntityUtils.POLICY_URI,
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida()
        );

        assertThat(request.getSignatureData()).isEqualTo(SIGNATURE_DATA);
        assertThat(request.getReferenceTimestamp()).isEqualTo(1751328000L);
        assertThat(request.getPolicyUri()).isEqualTo(EntityUtils.POLICY_URI);
        assertThat(request.getBundle()).isNotNull();
        assertThat(request.getProvenance()).isNotNull();
        assertThat(request.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("Deve construir com sucesso quando bundle e provenance são ambos nulos (sem verificação de integridade)")
    void verificationRequest_deveConstruirComSucesso_quandoBundleEProvenanceAmbosNulos() {
        VerificationRequest request = new VerificationRequest(
                SIGNATURE_DATA,
                1751328000L,
                EntityUtils.POLICY_URI,
                null,
                null
        );

        assertThat(request.getBundle()).isNull();
        assertThat(request.getProvenance()).isNull();
        assertThat(request.hasIntegrityCheck()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção quando signatureData é nula")
    void verificationRequest_deveLancarExcecao_quandoSignatureDataNula() {
        assertThatThrownBy(() -> new VerificationRequest(
                null, 1751328000L, EntityUtils.POLICY_URI, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("signatureData é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando signatureData contém apenas espaços em branco")
    void verificationRequest_deveLancarExcecao_quandoSignatureDataEmBranco() {
        assertThatThrownBy(() -> new VerificationRequest(
                "   ", 1751328000L, EntityUtils.POLICY_URI, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("signatureData é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri é nula")
    void verificationRequest_deveLancarExcecao_quandoPolicyUriNula() {
        assertThatThrownBy(() -> new VerificationRequest(
                SIGNATURE_DATA, 1751328000L, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando bundle está presente mas provenance é nula")
    void verificationRequest_deveLancarExcecao_quandoBundlePresenteEProvenanceNula() {
        assertThatThrownBy(() -> new VerificationRequest(
                SIGNATURE_DATA, 1751328000L, EntityUtils.POLICY_URI,
                EntityUtils.bundleDataValido(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle e provenance devem ser fornecidos em conjunto");
    }

    @Test
    @DisplayName("Deve lançar exceção quando provenance está presente mas bundle é nulo")
    void verificationRequest_deveLancarExcecao_quandoProvenancePresenteEBundleNulo() {
        assertThatThrownBy(() -> new VerificationRequest(
                SIGNATURE_DATA, 1751328000L, EntityUtils.POLICY_URI,
                null, EntityUtils.provenanceDataValida()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle e provenance devem ser fornecidos em conjunto");
    }
}