package com.runner.assinador.unit.domain.port.in;

import com.runner.assinador.domain.port.in.SignDocumentCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.domain.model.TimestampStrategy.IAT;
import static com.runner.assinador.domain.model.TimestampStrategy.TSA;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.cryptographicMaterialValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignDocumentCommandTest {

    private static final long TIMESTAMP_VALIDO = 1751328000L;

    @Test
    @DisplayName("Deve construir com sucesso quando todos os campos obrigatórios são válidos")
    void signDocumentCommand_deveConstruirComSucesso_quandoTodosCamposValidos() {
        SignDocumentCommand command = new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                POLICY_URI
        );

        assertThat(command.getBundle()).isNotNull();
        assertThat(command.getProvenance()).isNotNull();
        assertThat(command.getCryptographicMaterial()).isNotNull();
        assertThat(command.getReferenceTimestamp()).isEqualTo(TIMESTAMP_VALIDO);
        assertThat(command.getTimestampStrategy()).isEqualTo(IAT);
        assertThat(command.getPolicyUri()).isEqualTo(POLICY_URI);
    }

    @Test
    @DisplayName("Deve aceitar timestampStrategy TSA além de IAT")
    void signDocumentCommand_deveAceitarTimestampStrategy_quandoTSA() {
        SignDocumentCommand command = new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                TSA,
                POLICY_URI
        );

        assertThat(command.getTimestampStrategy()).isEqualTo(TSA);
    }

    @Test
    @DisplayName("Deve lançar exceção quando bundle é nulo")
    void signDocumentCommand_deveLancarExcecao_quandoBundleNulo() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                null,
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando provenance é nula")
    void signDocumentCommand_deveLancarExcecao_quandoProvenanceNula() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                null,
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("provenance é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando cryptographicMaterial é nulo")
    void signDocumentCommand_deveLancarExcecao_quandoCryptographicMaterialNulo() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                null,
                TIMESTAMP_VALIDO,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cryptographicMaterial é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp é nulo")
    void signDocumentCommand_deveLancarExcecao_quandoReferenceTimestampNulo() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                null,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp está abaixo do mínimo permitido")
    void signDocumentCommand_deveLancarExcecao_quandoReferenceTimestampAbaixoDoMinimo() {
        long abaixoDoMinimo = 1751327999L;

        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                abaixoDoMinimo,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp abaixo do mínimo permitido (01/07/2025)");
    }

    @Test
    @DisplayName("Deve lançar exceção quando referenceTimestamp está acima do máximo permitido")
    void signDocumentCommand_deveLancarExcecao_quandoReferenceTimestampAcimaDoMaximo() {
        long acimaDoMaximo = 4102444801L;

        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                acimaDoMaximo,
                IAT,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenceTimestamp acima do máximo permitido (31/12/2099)");
    }

    @Test
    @DisplayName("Deve aceitar referenceTimestamp exatamente igual ao limite mínimo")
    void signDocumentCommand_deveAceitarReferenceTimestamp_quandoIgualAoLimiteMinimo() {
        SignDocumentCommand command = new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                1751328000L,
                IAT,
                POLICY_URI
        );

        assertThat(command.getReferenceTimestamp()).isEqualTo(1751328000L);
    }

    @Test
    @DisplayName("Deve aceitar referenceTimestamp exatamente igual ao limite máximo")
    void signDocumentCommand_deveAceitarReferenceTimestamp_quandoIgualAoLimiteMaximo() {
        SignDocumentCommand command = new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                4102444800L,
                IAT,
                POLICY_URI
        );

        assertThat(command.getReferenceTimestamp()).isEqualTo(4102444800L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando timestampStrategy é nula")
    void signDocumentCommand_deveLancarExcecao_quandoTimestampStrategyNula() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                null,
                POLICY_URI))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("timestampStrategy é obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri é nula")
    void signDocumentCommand_deveLancarExcecao_quandoPolicyUriNula() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri não começa com https://")
    void signDocumentCommand_deveLancarExcecao_quandoPolicyUriNaoComecaComHttps() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                "http://fhir.saude.go.gov.br/policy|1.0.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando policyUri não contém versão semver")
    void signDocumentCommand_deveLancarExcecao_quandoPolicyUriSemVersao() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                "https://fhir.saude.go.gov.br/policy"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando versão da policyUri não segue o padrão major.minor.patch")
    void signDocumentCommand_deveLancarExcecao_quandoVersaoIncompleta() {
        assertThatThrownBy(() -> new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                "https://fhir.saude.go.gov.br/policy|1.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("policyUri deve seguir o formato https://<uri>|<major.minor.patch>");
    }

    @Test
    @DisplayName("Deve aceitar policyUri com versão major.minor.patch válida")
    void signDocumentCommand_deveAceitarPolicyUri_quandoVersaoValida() {
        String policyUri = "https://fhir.saude.go.gov.br/policy|10.20.30";

        SignDocumentCommand command = new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                TIMESTAMP_VALIDO,
                IAT,
                policyUri
        );

        assertThat(command.getPolicyUri()).isEqualTo(policyUri);
    }
}