package com.runner.assinador.unit.presentation.in.rest;

import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.SignDocumentUseCase;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import com.runner.assinador.presentation.in.rest.SignatureController;
import com.runner.assinador.presentation.in.rest.dto.request.SignRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.request.VerifyRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.response.SignResponseDTO;
import com.runner.assinador.presentation.in.rest.mapper.RestSignatureMapper;
import com.runner.assinador.presentation.shared.outcome.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.runner.assinador.utils.EntityUtils.signatureResultValido;
import static com.runner.assinador.utils.RestDTOUtils.signRequestDTOValido;
import static com.runner.assinador.utils.RestDTOUtils.verifyRequestDTOValidoComBundle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureControllerTest {

    @Mock
    private SignDocumentUseCase signDocumentUseCase;

    @Mock
    private VerifySignatureUseCase verifySignatureUseCase;

    @Mock
    private RestSignatureMapper mapper;

    private SignatureController controller;

    @BeforeEach
    void setUp() {
        controller = new SignatureController(signDocumentUseCase, verifySignatureUseCase, mapper);
    }

    @Test
    @DisplayName("sign deve retornar ResponseEntity com status 200 quando execução é bem sucedida")
    void sign_deveRetornarStatus200_quandoExecucaoBemSucedida() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);
        SignatureResult result = signatureResultValido();
        SignResponseDTO responseDTO = mock(SignResponseDTO.class);

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(result);
        when(mapper.toSignResponse(result)).thenReturn(responseDTO);

        ResponseEntity<SignResponseDTO> response = controller.sign(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("sign deve retornar no body o DTO produzido pelo mapper.toSignResponse")
    void sign_deveRetornarBodyComDtoProduzidoPeloMapper_quandoExecucaoBemSucedida() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);
        SignatureResult result = signatureResultValido();
        SignResponseDTO responseDTO = mock(SignResponseDTO.class);

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(result);
        when(mapper.toSignResponse(result)).thenReturn(responseDTO);

        ResponseEntity<SignResponseDTO> response = controller.sign(dto);

        assertThat(response.getBody()).isSameAs(responseDTO);
    }

    @Test
    @DisplayName("sign deve chamar mapper.toSignCommand com o DTO recebido")
    void sign_deveChamarMapperToSignCommand_comDtoRecebido() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(signatureResultValido());
        when(mapper.toSignResponse(any())).thenReturn(mock(SignResponseDTO.class));

        controller.sign(dto);

        verify(mapper).toSignCommand(dto);
    }

    @Test
    @DisplayName("sign deve chamar signDocumentUseCase.execute com o command produzido pelo mapper")
    void sign_deveChamarUseCaseExecute_comCommandProduzidoPeloMapper() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(signatureResultValido());
        when(mapper.toSignResponse(any())).thenReturn(mock(SignResponseDTO.class));

        controller.sign(dto);

        verify(signDocumentUseCase).execute(command);
    }

    @Test
    @DisplayName("sign deve chamar mapper.toSignResponse com o result devolvido pelo use case")
    void sign_deveChamarMapperToSignResponse_comResultDoUseCase() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);
        SignatureResult result = signatureResultValido();

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(result);
        when(mapper.toSignResponse(result)).thenReturn(mock(SignResponseDTO.class));

        controller.sign(dto);

        verify(mapper).toSignResponse(result);
    }

    @Test
    @DisplayName("sign não deve interagir com verifySignatureUseCase")
    void sign_naoDeveInteragirComVerifyUseCase() {
        SignRequestDTO dto = signRequestDTOValido();
        SignDocumentCommand command = mock(SignDocumentCommand.class);

        when(mapper.toSignCommand(dto)).thenReturn(command);
        when(signDocumentUseCase.execute(command)).thenReturn(signatureResultValido());
        when(mapper.toSignResponse(any())).thenReturn(mock(SignResponseDTO.class));

        controller.sign(dto);

        verifyNoInteractions(verifySignatureUseCase);
    }

    @Test
    @DisplayName("validate deve retornar ResponseEntity com status 200 quando verificação retorna sucesso")
    void validate_deveRetornarStatus200_quandoVerificacaoRetornaSucesso() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);
        VerificationResult result = VerificationResult.success("Assinatura válida");
        OperationOutcome outcome = mock(OperationOutcome.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(result);
        when(mapper.toOperationOutcome(result)).thenReturn(outcome);

        ResponseEntity<OperationOutcome> response = controller.validate(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("validate deve retornar status 200 mesmo quando verificação retorna falha (falha é resultado válido, não erro HTTP)")
    void validate_deveRetornarStatus200_mesmoQuandoVerificacaoRetornaFalha() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);
        VerificationResult result = VerificationResult.failure("FORMAT.JWS-MALFORMED", "JWS quebrado");
        OperationOutcome outcome = mock(OperationOutcome.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(result);
        when(mapper.toOperationOutcome(result)).thenReturn(outcome);

        ResponseEntity<OperationOutcome> response = controller.validate(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("validate deve retornar no body o OperationOutcome produzido pelo mapper.toOperationOutcome")
    void validate_deveRetornarBodyComOperationOutcome_quandoExecucaoBemSucedida() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);
        VerificationResult result = VerificationResult.success("ok");
        OperationOutcome outcome = mock(OperationOutcome.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(result);
        when(mapper.toOperationOutcome(result)).thenReturn(outcome);

        ResponseEntity<OperationOutcome> response = controller.validate(dto);

        assertThat(response.getBody()).isSameAs(outcome);
    }

    @Test
    @DisplayName("validate deve chamar mapper.toVerifyCommand com o DTO recebido")
    void validate_deveChamarMapperToVerifyCommand_comDtoRecebido() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(VerificationResult.success("ok"));
        when(mapper.toOperationOutcome(any())).thenReturn(mock(OperationOutcome.class));

        controller.validate(dto);

        verify(mapper).toVerifyCommand(dto);
    }

    @Test
    @DisplayName("validate deve chamar verifySignatureUseCase.execute com o command produzido pelo mapper")
    void validate_deveChamarUseCaseExecute_comCommandProduzidoPeloMapper() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(VerificationResult.success("ok"));
        when(mapper.toOperationOutcome(any())).thenReturn(mock(OperationOutcome.class));

        controller.validate(dto);

        verify(verifySignatureUseCase).execute(command);
    }

    @Test
    @DisplayName("validate deve chamar mapper.toOperationOutcome com o result devolvido pelo use case")
    void validate_deveChamarMapperToOperationOutcome_comResultDoUseCase() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);
        VerificationResult result = VerificationResult.success("ok");

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(result);
        when(mapper.toOperationOutcome(result)).thenReturn(mock(OperationOutcome.class));

        controller.validate(dto);

        verify(mapper).toOperationOutcome(result);
    }

    @Test
    @DisplayName("validate não deve interagir com signDocumentUseCase")
    void validate_naoDeveInteragirComSignUseCase() {
        VerifyRequestDTO dto = verifyRequestDTOValidoComBundle();
        VerifySignatureCommand command = mock(VerifySignatureCommand.class);

        when(mapper.toVerifyCommand(dto)).thenReturn(command);
        when(verifySignatureUseCase.execute(command)).thenReturn(VerificationResult.success("ok"));
        when(mapper.toOperationOutcome(any())).thenReturn(mock(OperationOutcome.class));

        controller.validate(dto);

        verifyNoInteractions(signDocumentUseCase);
    }
}