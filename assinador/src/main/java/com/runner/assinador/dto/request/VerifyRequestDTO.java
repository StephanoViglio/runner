package com.runner.assinador.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyRequestDTO {

    @NotBlank(message = "signatureData é obrigatório")
    private String signatureData;

    @NotNull(message = "referenceTimestamp é obrigatório")
    @Min(value = 1751328000L, message = "Timestamp abaixo do mínimo permitido (01/07/2025)")
    @Max(value = 4102444800L, message = "Timestamp acima do máximo permitido (31/12/2099)")
    private Long referenceTimestamp;

    @NotBlank(message = "policyUri é obrigatória")
    @Pattern(
            regexp = "^https://.+\\|\\d+\\.\\d+\\.\\d+$",
            message = "policyUri deve seguir o formato https://<uri>|<major.minor.patch>"
    )
    private String policyUri;

    @Valid
    private BundleDTO bundle;

    @Valid
    private ProvenanceDTO provenance;
}