package com.runner.assinador.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResourceEntryDTO {

    @NotBlank(message = "Informe o json da resource")
    private String resourceJson;

    private String uuid;
}