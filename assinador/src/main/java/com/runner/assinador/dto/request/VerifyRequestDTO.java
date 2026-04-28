package com.runner.assinador.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyRequestDTO {

    @NotBlank(message = "Assinatura deve ser informada")
    private String signatureData;

    @NotNull(message = "Timestamp de referência é obrigatório")
    @Min(value = 1751328000L, message = "Timestamp abaixo do mínimo permitido (01/07/2025)")
    @Max(value = 4102444800L, message = "Timestamp acima do máximo permitido (31/12/2099)")
    private Long referenceTimestamp;

    @NotBlank(message = "policyUri é obrigatória")
    @Pattern(
            regexp = "^https://fhir\\.saude\\.go\\.gov\\.br/r4/seguranca/ImplementationGuide/br\\.go\\.ses\\.seguranca\\|\\d+\\.\\d+\\.\\d+$",
            message = "policyUri deve seguir o formato {baseUri}|{major.minor.patch}"
    )
    private String policyUri;

    @Valid
    private BundleDTO bundle;

    @Valid
    private ProvenanceDTO provenance;
}