package com.runner.assinador.unit.presentation.in.cli.mapper;

import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.presentation.in.cli.input.SignInput;
import com.runner.assinador.presentation.in.cli.mapper.CliSignatureMapper;
import com.runner.assinador.presentation.in.cli.input.VerifyInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.runner.assinador.utils.CliInputUtils.SIGNATURE_DATA;
import static com.runner.assinador.utils.CliInputUtils.signInputValido;
import static com.runner.assinador.utils.CliInputUtils.verifyInputValidoComBundle;
import static com.runner.assinador.utils.CliInputUtils.verifyInputValidoSemBundle;
import static com.runner.assinador.utils.EntityUtils.CERT_BASE64;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.RESOURCE_JSON;
import static org.assertj.core.api.Assertions.assertThat;

class CliSignatureMapperTest {

    private CliSignatureMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CliSignatureMapper();
    }

    @Test
    @DisplayName("toSignCommand deve mapear todos os campos do SignInput para SignDocumentCommand")
    void toSignCommand_deveMapearTodosOsCampos_quandoInputValido() {
        SignInput input = signInputValido();

        SignDocumentCommand command = mapper.toSignCommand(input);

        assertThat(command.getReferenceTimestamp()).isEqualTo(input.getReferenceTimestamp());
        assertThat(command.getTimestampStrategy()).isEqualTo(input.getTimestampStrategy());
        assertThat(command.getPolicyUri()).isEqualTo(input.getPolicyUri());
    }

    @Test
    @DisplayName("toSignCommand deve mapear o bundle preservando fullUrl e resourceJson")
    void toSignCommand_deveMapearBundle_preservandoFullUrlEResourceJson() {
        SignInput input = signInputValido();

        SignDocumentCommand command = mapper.toSignCommand(input);

        assertThat(command.getBundle().getEntries()).hasSize(1);
        assertThat(command.getBundle().getEntries().get(0).getFullUrl()).isEqualTo(FULL_URL_PADRAO);
        assertThat(command.getBundle().getEntries().get(0).getResourceJson()).isEqualTo(RESOURCE_JSON);
    }

    @Test
    @DisplayName("toSignCommand deve mapear o provenance preservando os targets")
    void toSignCommand_deveMapearProvenance_preservandoTargets() {
        SignInput input = signInputValido();

        SignDocumentCommand command = mapper.toSignCommand(input);

        assertThat(command.getProvenance().getTargets()).containsExactly(FULL_URL_PADRAO);
    }

    @Test
    @DisplayName("toSignCommand deve mapear o cryptographicMaterial preservando todos os campos")
    void toSignCommand_deveMapearCryptographicMaterial_preservandoCampos() {
        SignInput input = signInputValido();

        SignDocumentCommand command = mapper.toSignCommand(input);

        assertThat(command.getCryptographicMaterial().getStrategy())
                .isEqualTo(input.getCryptographicMaterial().getCryptographicStrategy());
        assertThat(command.getCryptographicMaterial().getPin())
                .isEqualTo(input.getCryptographicMaterial().getPin());
        assertThat(command.getCryptographicMaterial().getIdentifier())
                .isEqualTo(input.getCryptographicMaterial().getIdentifier());
        assertThat(command.getCryptographicMaterial().getSlotId())
                .isEqualTo(input.getCryptographicMaterial().getSlotId());
        assertThat(command.getCryptographicMaterial().getTokenLabel())
                .isEqualTo(input.getCryptographicMaterial().getTokenLabel());
    }

    @Test
    @DisplayName("toSignCommand deve mapear certificateChain do input para o cryptographicMaterial")
    void toSignCommand_deveMapearCertificateChain_paraCryptographicMaterial() {
        SignInput input = signInputValido();

        SignDocumentCommand command = mapper.toSignCommand(input);

        assertThat(command.getCryptographicMaterial().getCertificateChain())
                .containsExactly(CERT_BASE64);
    }

    @Test
    @DisplayName("toVerifyCommand deve mapear todos os campos do VerifyInput para VerifySignatureCommand")
    void toVerifyCommand_deveMapearTodosOsCampos_quandoInputValidoComBundle() {
        VerifyInput input = verifyInputValidoComBundle();

        VerifySignatureCommand command = mapper.toVerifyCommand(input);

        assertThat(command.getSignatureData()).isEqualTo(SIGNATURE_DATA);
        assertThat(command.getReferenceTimestamp()).isEqualTo(input.getReferenceTimestamp());
        assertThat(command.getPolicyUri()).isEqualTo(POLICY_URI);
        assertThat(command.getBundle()).isNotNull();
        assertThat(command.getProvenance()).isNotNull();
        assertThat(command.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("toVerifyCommand deve construir command sem bundle e provenance quando input não os tem")
    void toVerifyCommand_deveConstruirCommandSemBundle_quandoInputSemBundle() {
        VerifyInput input = verifyInputValidoSemBundle();

        VerifySignatureCommand command = mapper.toVerifyCommand(input);

        assertThat(command.getBundle()).isNull();
        assertThat(command.getProvenance()).isNull();
        assertThat(command.hasIntegrityCheck()).isFalse();
    }
}