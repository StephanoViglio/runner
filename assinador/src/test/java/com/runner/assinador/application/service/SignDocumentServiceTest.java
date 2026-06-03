package com.runner.assinador.application.service;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.out.SignatureProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.runner.assinador.utils.EntityUtils.FULL_URL_2;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.RESOURCE_JSON;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.resourceEntryValida;
import static com.runner.assinador.utils.EntityUtils.signDocumentCommandValido;
import static com.runner.assinador.utils.EntityUtils.signatureResultValido;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignDocumentServiceTest {

    @Mock
    private SignatureProvider signatureProvider;

    private SignDocumentService service;

    @BeforeEach
    void setUp() {
        service = new SignDocumentService(signatureProvider);
    }

    @Test
    @DisplayName("Deve retornar o SignatureResult devolvido pelo provider quando todas as validações passam")
    void signDocumentService_deveRetornarSignatureResult_quandoTodasValidacoesPassam() {
        SignDocumentCommand command = signDocumentCommandValido();
        SignatureResult resultadoEsperado = signatureResultValido();
        when(signatureProvider.sign(any(SignatureRequest.class))).thenReturn(resultadoEsperado);

        SignatureResult resultado = service.execute(command);

        assertThat(resultado).isSameAs(resultadoEsperado);
    }

    @Test
    @DisplayName("Deve chamar signatureProvider.sign exatamente uma vez quando execução é bem sucedida")
    void signDocumentService_deveChamarProviderUmaVez_quandoExecucaoBemSucedida() {
        SignDocumentCommand command = signDocumentCommandValido();
        when(signatureProvider.sign(any(SignatureRequest.class))).thenReturn(signatureResultValido());

        service.execute(command);

        verify(signatureProvider).sign(any(SignatureRequest.class));
    }

    @Test
    @DisplayName("Deve construir SignatureRequest preservando todos os campos do command")
    void signDocumentService_deveConstruirSignatureRequest_preservandoCamposDoCommand() {
        SignDocumentCommand command = signDocumentCommandValido();
        when(signatureProvider.sign(any(SignatureRequest.class))).thenReturn(signatureResultValido());

        service.execute(command);

        ArgumentCaptor<SignatureRequest> captor = ArgumentCaptor.forClass(SignatureRequest.class);
        verify(signatureProvider).sign(captor.capture());
        SignatureRequest request = captor.getValue();

        assertThat(request.getBundle()).isSameAs(command.getBundle());
        assertThat(request.getProvenance()).isSameAs(command.getProvenance());
        assertThat(request.getCryptographicMaterial()).isSameAs(command.getCryptographicMaterial());
        assertThat(request.getReferenceTimestamp()).isEqualTo(command.getReferenceTimestamp());
        assertThat(request.getTimestampStrategy()).isEqualTo(command.getTimestampStrategy());
        assertThat(request.getPolicyUri()).isEqualTo(command.getPolicyUri());
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código TIMESTAMP_OUT_OF_TOLERANCE_WINDOW quando timestamp está muito no passado")
    void signDocumentService_deveLancarExcecao_quandoTimestampMuitoNoPassado() {
        long timestampMuitoAntigo = Instant.now().getEpochSecond() - 600L;
        SignDocumentCommand command = signDocumentCommandValido(timestampMuitoAntigo);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW);
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código TIMESTAMP_OUT_OF_TOLERANCE_WINDOW quando timestamp está muito no futuro")
    void signDocumentService_deveLancarExcecao_quandoTimestampMuitoNoFuturo() {
        long timestampMuitoNoFuturo = Instant.now().getEpochSecond() + 600L;
        SignDocumentCommand command = signDocumentCommandValido(timestampMuitoNoFuturo);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.TIMESTAMP_OUT_OF_TOLERANCE_WINDOW);
    }

    @Test
    @DisplayName("Deve aceitar timestamp dentro da janela de tolerância de 5 minutos no passado")
    void signDocumentService_deveAceitarTimestamp_quandoDentroDaJanelaDePassado() {
        long timestampPassadoAceitavel = Instant.now().getEpochSecond() - 60L;
        SignDocumentCommand command = signDocumentCommandValido(timestampPassadoAceitavel);
        when(signatureProvider.sign(any(SignatureRequest.class))).thenReturn(signatureResultValido());

        SignatureResult resultado = service.execute(command);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar timestamp dentro da janela de tolerância de 5 minutos no futuro")
    void signDocumentService_deveAceitarTimestamp_quandoDentroDaJanelaDeFuturo() {
        long timestampFuturoAceitavel = Instant.now().getEpochSecond() + 60L;
        SignDocumentCommand command = signDocumentCommandValido(timestampFuturoAceitavel);
        when(signatureProvider.sign(any(SignatureRequest.class))).thenReturn(signatureResultValido());

        SignatureResult resultado = service.execute(command);

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código FORMAT_DUPLICATE_FULLURL quando bundle tem fullUrls duplicados")
    void signDocumentService_deveLancarExcecao_quandoBundleTemFullUrlsDuplicados() {
        BundleData bundleComDuplicata = new BundleData(List.of(
                resourceEntryValida(FULL_URL_PADRAO),
                new ResourceEntry(FULL_URL_PADRAO, RESOURCE_JSON)
        ));
        SignDocumentCommand command = signDocumentCommandValido(bundleComDuplicata);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_DUPLICATE_FULLURL);
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código FORMAT_PROVENANCE_TARGET_DUPLICATE quando provenance tem targets duplicados")
    void signDocumentService_deveLancarExcecao_quandoProvenanceTemTargetsDuplicados() {
        ProvenanceData provenanceComDuplicata = new ProvenanceData(List.of(
                FULL_URL_PADRAO,
                FULL_URL_PADRAO
        ));
        SignDocumentCommand command = signDocumentCommandValido(bundleDataValido(), provenanceComDuplicata);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_PROVENANCE_TARGET_DUPLICATE);
    }

    @Test
    @DisplayName("Deve lançar SignatureException com código FORMAT_TARGET_REFERENCE_MISSING quando provenance referencia entry inexistente")
    void signDocumentService_deveLancarExcecao_quandoProvenanceReferenciaEntryInexistente() {
        ProvenanceData provenanceComReferenciaInvalida = new ProvenanceData(List.of(FULL_URL_2));
        SignDocumentCommand command = signDocumentCommandValido(bundleDataValido(), provenanceComReferenciaInvalida);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_TARGET_REFERENCE_MISSING);
    }

    @Test
    @DisplayName("Não deve chamar o signatureProvider quando uma validação falha")
    void signDocumentService_naoDeveChamarProvider_quandoValidacaoFalha() {
        long timestampInvalido = Instant.now().getEpochSecond() - 600L;
        SignDocumentCommand command = signDocumentCommandValido(timestampInvalido);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SignatureException.class);

        verify(signatureProvider, never()).sign(any(SignatureRequest.class));
    }
}