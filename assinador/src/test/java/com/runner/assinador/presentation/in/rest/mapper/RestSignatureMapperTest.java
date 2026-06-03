package com.runner.assinador.presentation.in.rest.mapper;

import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.presentation.in.rest.dto.request.SignRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.request.VerifyRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.response.SignResponseDTO;
import com.runner.assinador.presentation.shared.outcome.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.utils.EntityUtils.CERT_BASE64;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.RESOURCE_JSON;
import static com.runner.assinador.utils.EntityUtils.signatureResultValido;
import static com.runner.assinador.utils.RestDTOUtils.SIGNATURE_DATA;
import static com.runner.assinador.utils.RestDTOUtils.signRequestDTOValido;
import static com.runner.assinador.utils.RestDTOUtils.verifyRequestDTOValidoComBundle;
import static com.runner.assinador.utils.RestDTOUtils.verifyRequestDTOValidoSemBundle;
import static org.assertj.core.api.Assertions.assertThat;

class RestSignatureMapperTest {

    private RestSignatureMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RestSignatureMapper();
    }

    @Test
    @DisplayName("toSignCommand deve mapear todos os campos do SignRequestDTO para SignDocumentCommand")
    void toSignCommand_deveMapearTodosOsCampos_quandoDTOValido() {
        SignRequestDTO dto = signRequestDTOValido();

        SignDocumentCommand command = mapper.toSignCommand(dto);

        assertThat(command.getReferenceTimestamp()).isEqualTo(dto.getReferenceTimestamp());
        assertThat(command.getTimestampStrategy()).isEqualTo(dto.getTimestampStrategy());
        assertThat(command.getPolicyUri()).isEqualTo(dto.getPolicyUri());
    }

    @Test
    @DisplayName("toSignCommand deve mapear o bundle preservando fullUrl e resourceJson")
    void toSignCommand_deveMapearBundle_preservandoFullUrlEResourceJson() {
        SignRequestDTO dto = signRequestDTOValido();

        SignDocumentCommand command = mapper.toSignCommand(dto);

        assertThat(command.getBundle().getEntries()).hasSize(1);
        assertThat(command.getBundle().getEntries().get(0).getFullUrl()).isEqualTo(FULL_URL_PADRAO);
        assertThat(command.getBundle().getEntries().get(0).getResourceJson()).isEqualTo(RESOURCE_JSON);
    }

    @Test
    @DisplayName("toSignCommand deve mapear o provenance preservando os targets")
    void toSignCommand_deveMapearProvenance_preservandoTargets() {
        SignRequestDTO dto = signRequestDTOValido();

        SignDocumentCommand command = mapper.toSignCommand(dto);

        assertThat(command.getProvenance().getTargets()).containsExactly(FULL_URL_PADRAO);
    }

    @Test
    @DisplayName("toSignCommand deve mapear o cryptographicMaterial preservando todos os campos")
    void toSignCommand_deveMapearCryptographicMaterial_preservandoCampos() {
        SignRequestDTO dto = signRequestDTOValido();

        SignDocumentCommand command = mapper.toSignCommand(dto);

        assertThat(command.getCryptographicMaterial().getStrategy())
                .isEqualTo(dto.getCryptographicMaterial().getCryptographicStrategy());
        assertThat(command.getCryptographicMaterial().getPin())
                .isEqualTo(dto.getCryptographicMaterial().getPin());
        assertThat(command.getCryptographicMaterial().getIdentifier())
                .isEqualTo(dto.getCryptographicMaterial().getIdentifier());
        assertThat(command.getCryptographicMaterial().getSlotId())
                .isEqualTo(dto.getCryptographicMaterial().getSlotId());
        assertThat(command.getCryptographicMaterial().getTokenLabel())
                .isEqualTo(dto.getCryptographicMaterial().getTokenLabel());
    }

    @Test
    @DisplayName("toSignCommand deve mapear certificateChain do DTO para o cryptographicMaterial")
    void toSignCommand_deveMapearCertificateChain_paraCryptographicMaterial() {
        SignRequestDTO dto = signRequestDTOValido();

        SignDocumentCommand command = mapper.toSignCommand(dto);

        assertThat(command.getCryptographicMaterial().getCertificateChain())
                .containsExactly(CERT_BASE64);
    }

    @Test
    @DisplayName("toVerifyCommand deve mapear todos os campos do VerifyRequestDTO para VerifySignatureCommand")
    void toVerifyCommand_deveMapearTodosOsCampos_quandoDTOValidoComBundle() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();

        VerifySignatureCommand command = mapper.toVerifyCommand(dto);

        assertThat(command.getSignatureData()).isEqualTo(SIGNATURE_DATA);
        assertThat(command.getReferenceTimestamp()).isEqualTo(dto.getReferenceTimestamp());
        assertThat(command.getPolicyUri()).isEqualTo(POLICY_URI);
        assertThat(command.getBundle()).isNotNull();
        assertThat(command.getProvenance()).isNotNull();
        assertThat(command.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("toVerifyCommand deve construir command sem bundle e provenance quando DTO não os tem")
    void toVerifyCommand_deveConstruirCommandSemBundle_quandoDTOSemBundle() {
        VerifyRequestDTO dto = verifyRequestDTOValidoSemBundle();

        VerifySignatureCommand command = mapper.toVerifyCommand(dto);

        assertThat(command.getBundle()).isNull();
        assertThat(command.getProvenance()).isNull();
        assertThat(command.hasIntegrityCheck()).isFalse();
    }

    @Test
    @DisplayName("toSignResponse deve mapear o SignatureResult para SignResponseDTO preservando os campos simples")
    void toSignResponse_deveMapearCamposSimples_quandoSignatureResultValido() {
        SignatureResult result = signatureResultValido();

        SignResponseDTO response = mapper.toSignResponse(result);

        assertThat(response.getWhen()).isEqualTo(result.getWhen());
        assertThat(response.getTargetFormat()).isEqualTo(result.getTargetFormat());
        assertThat(response.getSigFormat()).isEqualTo(result.getSigFormat());
        assertThat(response.getData()).isEqualTo(result.getData());
    }

    @Test
    @DisplayName("toSignResponse deve mapear a lista de codings preservando system e code")
    void toSignResponse_deveMapearListaDeCodings_preservandoSystemECode() {
        SignatureResult result = signatureResultValido();

        SignResponseDTO response = mapper.toSignResponse(result);

        assertThat(response.getType()).hasSize(1);
        assertThat(response.getType().get(0).getSystem()).isEqualTo(result.getType().get(0).system());
        assertThat(response.getType().get(0).getCode()).isEqualTo(result.getType().get(0).code());
    }

    @Test
    @DisplayName("toSignResponse deve construir o who com system urn:brasil:cpf e o cpf do resultado")
    void toSignResponse_deveConstruirWho_comSystemUrnBrasilCpf() {
        SignatureResult result = signatureResultValido();

        SignResponseDTO response = mapper.toSignResponse(result);

        assertThat(response.getWho().getIdentifier().getSystem()).isEqualTo("urn:brasil:cpf");
        assertThat(response.getWho().getIdentifier().getValue()).isEqualTo(result.getSignerCpf());
    }

    @Test
    @DisplayName("toOperationOutcome deve mapear VerificationResult de sucesso para OperationOutcome com code VALIDATION.SUCCESS")
    void toOperationOutcome_deveMapearSucesso_paraOperationOutcomeComCodeValidationSuccess() {
        com.runner.assinador.domain.model.VerificationResult result =
                com.runner.assinador.domain.model.VerificationResult.success("Assinatura válida");

        OperationOutcome outcome = mapper.toOperationOutcome(result);

        assertThat(outcome.getIssues()).hasSize(1);
        assertThat(outcome.getIssues().get(0).getDetails().getCoding().get(0).getCode())
                .isEqualTo("VALIDATION.SUCCESS");
        assertThat(outcome.getIssues().get(0).getDiagnostics()).isEqualTo("Assinatura válida");
    }

    @Test
    @DisplayName("toOperationOutcome deve mapear VerificationResult de falha para OperationOutcome com o código de erro")
    void toOperationOutcome_deveMapearFalha_paraOperationOutcomeComCodigoDeErro() {
        com.runner.assinador.domain.model.VerificationResult result =
                com.runner.assinador.domain.model.VerificationResult.failure(
                        "FORMAT.JWS-MALFORMED", "JWS quebrado");

        OperationOutcome outcome = mapper.toOperationOutcome(result);

        assertThat(outcome.getIssues()).hasSize(1);
        assertThat(outcome.getIssues().get(0).getDetails().getCoding().get(0).getCode())
                .isEqualTo("FORMAT.JWS-MALFORMED");
        assertThat(outcome.getIssues().get(0).getDiagnostics()).isEqualTo("JWS quebrado");
    }
}