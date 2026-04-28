package com.runner.assinador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResourceEntryDTO {

    @NotBlank(message = "Informe o fullUrl da entry (formato: urn:uuid:<UUID>)")
    @Pattern(
            regexp = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "fullUrl deve seguir o formato urn:uuid:<UUID RFC 4122>"
    )
    private String fullUrl;

    @NotBlank(message = "Informe o JSON da resource")
    private String resourceJson;
}