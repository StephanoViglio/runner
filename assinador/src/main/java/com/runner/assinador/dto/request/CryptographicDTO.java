package com.runner.assinador.dto.request;

import com.runner.assinador.utils.CryptographicStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CryptographicDTO {

    @NotNull(message = "Informe a estratégia de criptografia que será utilizada")
    private CryptographicStrategy cryptographicStrategy;

    @NotBlank(message = "Informe o PIN da criptografia")
    private String pin;

    @NotBlank(message = "Informe o identificador da criptografia")
    private String identifier;

    @PositiveOrZero(message = "Slot ID deve ser um valor inteiro não negativo (0 é válido)")
    private Integer slotId;

    @Size(max = 32, message = "Token label deve ter no máximo 32 caracteres")
    private String tokenLabel;
}