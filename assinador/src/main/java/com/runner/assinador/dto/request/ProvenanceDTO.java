package com.runner.assinador.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class ProvenanceDTO {

    @NotEmpty(message = "Informe ao menos um target")
    private List<
            @Pattern(
                    regexp = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                    message = "Cada target deve seguir o formato urn:uuid:<UUID RFC 4122>"
            ) String> target;
}