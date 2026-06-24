package com.runner.assinador.unit.domain.model;

import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignatureRequestTest {

    @Test
    @DisplayName("Deve construir com sucesso quando todos os campos obrigatórios são válidos")
    void signatureRequest_deveConstruirComSucesso_quandoTodosCamposValidos() {
        SignatureRequest request = new SignatureRequest(
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida(),
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                TimestampStrategy.IAT,
                EntityUtils.POLICY_URI
        );

        assertThat(request.getBundle()).isNotNull();
        assertThat(request.getProvenance()).isNotNull();
        assertThat(request.getCryptographicMaterial()).isNotNull();
        assertThat(request.getReferenceTimestamp()).isEqualTo(1751328000L);
        assertThat(request.getTimestampStrategy()).isEqualTo(TimestampStrategy.IAT);
        assertThat(request.getPolicyUri()).isEqualTo(EntityUtils.POLICY_URI);
    }

    @Test
    @DisplayName("Deve lançar exceção quando bundle é nulo")
    void signatureRequest_deveLancarExcecao_quandoBundleNulo() {
        assertThatThrownBy(() -> new SignatureRequest(
                null,
                EntityUtils.provenanceDataValida(),
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                TimestampStrategy.IAT,
                EntityUtils.POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando provenance é nula")
    void signatureRequest_deveLancarExcecao_quandoProvenanceNula() {
        assertThatThrownBy(() -> new SignatureRequest(
                EntityUtils.bundleDataValido(),
                null,
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                TimestampStrategy.IAT,
                EntityUtils.POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("provenance é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando cryptographicMaterial é nulo")
    void signatureRequest_deveLancarExcecao_quandoCryptographicMaterialNulo() {
        assertThatThrownBy(() -> new SignatureRequest(
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida(),
                null,
                1751328000L,
                TimestampStrategy.IAT,
                EntityUtils.POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cryptographicMaterial é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando timestampStrategy é nula")
    void signatureRequest_deveLancarExcecao_quandoTimestampStrategyNula() {
        assertThatThrownBy(() -> new SignatureRequest(
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida(),
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                null,
                EntityUtils.POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("timestampStrategy é obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri é nula")
    void signatureRequest_deveLancarExcecao_quandoPolicyUriNula() {
        assertThatThrownBy(() -> new SignatureRequest(
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida(),
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                TimestampStrategy.IAT,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri contém apenas espaços em branco")
    void signatureRequest_deveLancarExcecao_quandoPolicyUriEmBranco() {
        assertThatThrownBy(() -> new SignatureRequest(
                EntityUtils.bundleDataValido(),
                EntityUtils.provenanceDataValida(),
                EntityUtils.cryptographicMaterialValido(),
                1751328000L,
                TimestampStrategy.IAT,
                "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri é obrigatório");
    }
}