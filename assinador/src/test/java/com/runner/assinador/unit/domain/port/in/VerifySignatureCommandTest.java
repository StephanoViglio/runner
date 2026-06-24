package com.runner.assinador.unit.domain.port.in;

import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerifySignatureCommandTest {

    private static final String SIGNATURE_DATA = "U0lHTkFUVVJFX0RBVEFfQkFTRTY0";
    private static final long TIMESTAMP_VALIDO = 1751328000L;

    @Test
    @DisplayName("Deve construir com sucesso quando todos os campos obrigatórios são válidos e bundle+provenance presentes")
    void verifySignatureCommand_deveConstruirComSucesso_quandoTodosCamposValidos() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );

        assertThat(command.getSignatureData()).isEqualTo(SIGNATURE_DATA);
        assertThat(command.getReferenceTimestamp()).isEqualTo(TIMESTAMP_VALIDO);
        assertThat(command.getPolicyUri()).isEqualTo(POLICY_URI);
        assertThat(command.getBundle()).isNotNull();
        assertThat(command.getProvenance()).isNotNull();
        assertThat(command.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("Deve construir com sucesso quando bundle e provenance são ambos nulos (sem verificação de integridade)")
    void verifySignatureCommand_deveConstruirComSucesso_quandoBundleEProvenanceNulos() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                null
        );

        assertThat(command.getBundle()).isNull();
        assertThat(command.getProvenance()).isNull();
        assertThat(command.hasIntegrityCheck()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção quando signatureData é nula")
    void verifySignatureCommand_deveLancarExcecao_quandoSignatureDataNula() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                null,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("signatureData é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando signatureData é string vazia")
    void verifySignatureCommand_deveLancarExcecao_quandoSignatureDataVazia() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                "",
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("signatureData é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando signatureData contém apenas espaços em branco")
    void verifySignatureCommand_deveLancarExcecao_quandoSignatureDataEmBranco() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                "   ",
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("signatureData é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp é nulo")
    void verifySignatureCommand_deveLancarExcecao_quandoReferenceTimestampNulo() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                null,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp está abaixo do mínimo permitido")
    void verifySignatureCommand_deveLancarExcecao_quandoReferenceTimestampAbaixoDoMinimo() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                1751327999L,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp abaixo do mínimo permitido (01/07/2025)");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp está acima do máximo permitido")
    void verifySignatureCommand_deveLancarExcecao_quandoReferenceTimestampAcimaDoMaximo() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                4102444801L,
                POLICY_URI,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp acima do máximo permitido (31/12/2099)");
    }

    @Test
    @DisplayName("Deve aceitar referenceTimestamp exatamente igual ao limite mínimo")
    void verifySignatureCommand_deveAceitarReferenceTimestamp_quandoIgualAoLimiteMinimo() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                1751328000L,
                POLICY_URI,
                null,
                null
        );

        assertThat(command.getReferenceTimestamp()).isEqualTo(1751328000L);
    }

    @Test
    @DisplayName("Deve aceitar referenceTimestamp exatamente igual ao limite máximo")
    void verifySignatureCommand_deveAceitarReferenceTimestamp_quandoIgualAoLimiteMaximo() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                4102444800L,
                POLICY_URI,
                null,
                null
        );

        assertThat(command.getReferenceTimestamp()).isEqualTo(4102444800L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri é nula")
    void verifySignatureCommand_deveLancarExcecao_quandoPolicyUriNula() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                null,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri não segue o formato esperado")
    void verifySignatureCommand_deveLancarExcecao_quandoPolicyUriComFormatoInvalido() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                "http://fhir.saude.go.gov.br/policy|1.0.0",
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando bundle está presente mas provenance é nula")
    void verifySignatureCommand_deveLancarExcecao_quandoBundlePresenteEProvenanceNula() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                bundleDataValido(),
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle e provenance devem ser fornecidos em conjunto");
    }

    @Test
    @DisplayName("Deve lançar exceção quando provenance está presente mas bundle é nulo")
    void verifySignatureCommand_deveLancarExcecao_quandoProvenancePresenteEBundleNulo() {
        assertThatThrownBy(() -> new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                provenanceDataValida()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle e provenance devem ser fornecidos em conjunto");
    }

    @Test
    @DisplayName("hasIntegrityCheck deve retornar true quando bundle está presente")
    void verifySignatureCommand_hasIntegrityCheck_deveRetornarTrue_quandoBundlePresente() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );

        assertThat(command.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("hasIntegrityCheck deve retornar false quando bundle está ausente")
    void verifySignatureCommand_hasIntegrityCheck_deveRetornarFalse_quandoBundleAusente() {
        VerifySignatureCommand command = new VerifySignatureCommand(
                SIGNATURE_DATA,
                TIMESTAMP_VALIDO,
                POLICY_URI,
                null,
                null
        );

        assertThat(command.hasIntegrityCheck()).isFalse();
    }
}