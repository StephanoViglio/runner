package com.runner.assinador.controller;

import com.runner.assinador.dto.request.SignRequestDTO;
import com.runner.assinador.dto.request.VerifyRequestDTO;
import com.runner.assinador.dto.response.SignResponseDTO;
import com.runner.assinador.dto.response.VerifyResponseDTO;
import com.runner.assinador.service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final SignatureService signatureService;

    @Operation(summary = "Criar assinatura digital",
            description = """
                Cria uma assinatura digital no padrão JAdES/JWS sobre um conjunto de dados.
        
                Os dados a serem assinados e sua ordem são fornecidos em
                `signingBundle.resources`. O serviço organiza e prepara essas informações
                internamente antes da assinatura.
        
                A assinatura é realizada por um dispositivo PKCS#11 (SMARTCARD ou TOKEN)
                através do provider SunPKCS11. A chave privada nunca sai do hardware.
        
                Retorna os dados da assinatura em caso de sucesso,
                ou detalhes do erro em caso de falha.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Assinatura criada com sucesso"),
                    @ApiResponse(responseCode = "422", description = "Entrada inválida"),
                    @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
            }
    )
    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignResponseDTO> sign(@RequestBody SignRequestDTO request) {
        SignResponseDTO response = signatureService.sign(request);

        log.info("POST /sign — done. algorithm: {}", response.getAlgorithm());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validar assinatura digital",
            description = """
                Valida uma assinatura digital no padrão JAdES/JWS com base nos dados originais.
        
                O validador reconstrói o hash SHA-256 a partir dos dados fornecidos em
                `signingBundle.resources` (na mesma ordem utilizada durante a assinatura),
                e verifica a assinatura utilizando a chave pública presente
                no header x5c.
        
                Não é necessário dispositivo PKCS#11 para validação.
        
                Retorna o resultado da validação em caso de sucesso,
                ou detalhes do erro em caso de falha.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado da validação"),
                    @ApiResponse(responseCode = "422", description = "Entrada inválida"),
                    @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
            }
    )
    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifyResponseDTO> validate(@RequestBody VerifyRequestDTO request) {
        VerifyResponseDTO response = signatureService.verify(request);

        log.info("POST /validate — done. valid: {}", response.isValid());
        return ResponseEntity.ok(response);
    }
}