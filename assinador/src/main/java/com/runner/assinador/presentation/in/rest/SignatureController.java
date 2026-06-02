package com.runner.assinador.presentation.in.rest;

import com.runner.assinador.presentation.in.rest.dto.request.SignRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.request.VerifyRequestDTO;
import com.runner.assinador.presentation.shared.outcome.OperationOutcome;
import com.runner.assinador.presentation.in.rest.dto.response.SignResponseDTO;
import com.runner.assinador.presentation.in.rest.mapper.RestSignatureMapper;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.VerificationResult;
import com.runner.assinador.domain.port.in.SignDocumentUseCase;
import com.runner.assinador.domain.port.in.VerifySignatureUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Signature", description = "Operações de assinatura digital")
public class SignatureController {

    private final SignDocumentUseCase signDocumentUseCase;
    private final VerifySignatureUseCase verifySignatureUseCase;
    private final RestSignatureMapper mapper;

    @Operation(summary = "Criar assinatura digital", responses = {
            @ApiResponse(responseCode = "200", description = "Assinatura criada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Entrada inválida"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PostMapping(value = "/sign",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignResponseDTO> sign(@RequestBody @Valid SignRequestDTO request) {
        SignDocumentCommand command = mapper.toSignCommand(request);
        SignatureResult result      = signDocumentUseCase.execute(command);
        return ResponseEntity.ok(mapper.toSignResponse(result));
    }

    @Operation(summary = "Validar assinatura digital", responses = {
            @ApiResponse(responseCode = "200", description = "Resultado da validação"),
            @ApiResponse(responseCode = "422", description = "Entrada inválida"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PostMapping(value = "/validate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OperationOutcome> validate(@RequestBody @Valid VerifyRequestDTO request) {
        VerifySignatureCommand command  = mapper.toVerifyCommand(request);
        VerificationResult result       = verifySignatureUseCase.execute(command);
        return ResponseEntity.ok(mapper.toOperationOutcome(result));
    }
}