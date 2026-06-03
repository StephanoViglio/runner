package com.runner.assinador.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationResultTest {

    @Test
    @DisplayName("Factory success deve produzir um resultado de sucesso com código VALIDATION.SUCCESS")
    void verificationResult_deveProduzirSucesso_quandoFactorySuccessChamada() {
        VerificationResult result = VerificationResult.success("Assinatura válida");

        assertThat(result.getStatus()).isEqualTo(VerificationResult.Status.SUCCESS);
        assertThat(result.getCode()).isEqualTo("VALIDATION.SUCCESS");
        assertThat(result.getMessage()).isEqualTo("Assinatura válida");
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Factory failure deve produzir um resultado de falha com código e mensagem fornecidos")
    void verificationResult_deveProduzirFalha_quandoFactoryFailureChamada() {
        VerificationResult result = VerificationResult.failure("FORMAT.JWS-MALFORMED", "JWS quebrado");

        assertThat(result.getStatus()).isEqualTo(VerificationResult.Status.FAILURE);
        assertThat(result.getCode()).isEqualTo("FORMAT.JWS-MALFORMED");
        assertThat(result.getMessage()).isEqualTo("JWS quebrado");
        assertThat(result.isSuccess()).isFalse();
    }
}