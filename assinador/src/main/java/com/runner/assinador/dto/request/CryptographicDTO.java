package com.runner.assinador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "Informe o PIN da criptografia")
    private String pin;

    @NotBlank(message = "Informe o identificador da criptografia")
    private String identifier;

    @NotNull(message = "Informe o Slot ID da criptografia")
    @Positive(message = "Slot ID deve ser um valor positivo")
    private Integer slotId;

    @Size(max = 32, message = "Token label deve ter no máximo 32 caracteres")
    @NotBlank(message = "Informe o token label da criptografia")
    private String tokenLabel;
}
