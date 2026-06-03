package com.runner.assinador.application.service;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.domain.port.out.SignatureProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.runner.assinador.utils.EntityUtils.verifySignatureCommandValido;
import static com.runner.assinador.utils.EntityUtils.verifySignatureCommandValidoSemBundle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifySignatureServiceTest {

    @Mock
    private SignatureProvider signatureProvider;

    private VerifySignatureService service;

    @BeforeEach
    void setUp() {
        service = new VerifySignatureService(signatureProvider);
    }

    @Test
    @DisplayName("Deve retornar VerificationResult devolvido pelo provider quando todas as validações passam")
    void verifySignatureService_deveRetornarVerificationResult_quandoTodasValidacoesPassam() {
        VerifySignatureCommand command = verifySignatureCommandValido();
        VerificationResult esperado = VerificationResult.success("Assinatura válida");
        when(signatureProvider.verify(any(VerificationRequest.class))).thenReturn(esperado);

        VerificationResult resultado = service.execute(command);

        assertThat(resultado).isSameAs(esperado);
    }

    @Test
    @DisplayName("Deve propagar resultado de falha devolvido pelo provider")
    void verifySignatureService_devePropagarFalha_quandoProviderRetornaFailure() {
        VerifySignatureCommand command = verifySignatureCommandValido();
        VerificationResult esperado = VerificationResult.failure("FORMAT.JWS-MALFORMED", "JWS quebrado");
        when(signatureProvider.verify(any(VerificationRequest.class))).thenReturn(esperado);

        VerificationResult resultado = service.execute(command);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getCode()).isEqualTo("FORMAT.JWS-MALFORMED");
    }

    @Test
    @DisplayName("Deve chamar signatureProvider.verify exatamente uma vez quando execução é bem sucedida")
    void verifySignatureService_deveChamarProviderUmaVez_quandoExecucaoBemSucedida() {
        VerifySignatureCommand command = verifySignatureCommandValido();
        when(signatureProvider.verify(any(VerificationRequest.class)))
                .thenReturn(VerificationResult.success("ok"));

        service.execute(command);

        verify(signatureProvider).verify(any(VerificationRequest.class));
    }

    @Test
    @DisplayName("Deve construir VerificationRequest preservando todos os campos do command")
    void verifySignatureService_deveConstruirVerificationRequest_preservandoCamposDoCommand() {
        VerifySignatureCommand command = verifySignatureCommandValido();
        when(signatureProvider.verify(any(VerificationRequest.class)))
                .thenReturn(VerificationResult.success("ok"));

        service.execute(command);

        ArgumentCaptor<VerificationRequest> captor = ArgumentCaptor.forClass(VerificationRequest.class);
        verify(signatureProvider).verify(captor.capture());
        VerificationRequest request = captor.getValue();

        assertThat(request.getSignatureData()).isEqualTo(command.getSignatureData());
        assertThat(request.getReferenceTimestamp()).isEqualTo(command.getReferenceTimestamp());
        assertThat(request.getPolicyUri()).isEqualTo(command.getPolicyUri());
        assertThat(request.getBundle()).isSameAs(command.getBundle());
        assertThat(request.getProvenance()).isSameAs(command.getProvenance());
        assertThat(request.hasIntegrityCheck()).isTrue();
    }

    @Test
    @DisplayName("Deve construir VerificationRequest sem bundle e provenance quando o command não os tem")
    void verifySignatureService_deveConstruirVerificationRequestSemIntegrityCheck_quandoCommandSemBundle() {
        VerifySignatureCommand command = verifySignatureCommandValidoSemBundle();
        when(signatureProvider.verify(any(VerificationRequest.class)))
                .thenReturn(VerificationResult.success("ok"));

        service.execute(command);

        ArgumentCaptor<VerificationRequest> captor = ArgumentCaptor.forClass(VerificationRequest.class);
        verify(signatureProvider).verify(captor.capture());
        VerificationRequest request = captor.getValue();

        assertThat(request.getBundle()).isNull();
        assertThat(request.getProvenance()).isNull();
        assertThat(request.hasIntegrityCheck()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código TIMESTAMP_OUT_OF_TOLERANCE_WINDOW quando timestamp está muito no passado")
    void verifySignatureService_deveLancarExcecao_quandoTimestampMuitoNoPassado() {
        long timestampInvalido = Instant.now().getEpochSecond() - 600L;
        VerifySignatureCommand command = verifySignatureCommandValido(timestampInvalido);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW);
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código TIMESTAMP_OUT_OF_TOLERANCE_WINDOW quando timestamp está muito no futuro")
    void verifySignatureService_deveLancarExcecao_quandoTimestampMuitoNoFuturo() {
        long timestampInvalido = Instant.now().getEpochSecond() + 600L;
        VerifySignatureCommand command = verifySignatureCommandValido(timestampInvalido);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW);
    }

    @Test
    @DisplayName("Não deve chamar o signatureProvider quando a validação de timestamp falha")
    void verifySignatureService_naoDeveChamarProvider_quandoValidacaoFalha() {
        long timestampInvalido = Instant.now().getEpochSecond() - 600L;
        VerifySignatureCommand command = verifySignatureCommandValido(timestampInvalido);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class);

        verify(signatureProvider, never()).verify(any(VerificationRequest.class));
    }
}