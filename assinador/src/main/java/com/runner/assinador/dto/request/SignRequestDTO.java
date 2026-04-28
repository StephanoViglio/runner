package com.runner.assinador.dto.request;

import com.runner.assinador.utils.TimestampStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SignRequestDTO {

    @NotNull(message = "bundle é obrigatório")
    @Valid
    private BundleDTO bundle;

    @NotNull(message = "provenance é obrigatório")
    @Valid
    private ProvenanceDTO provenance;

    @NotNull(message = "cryptographicMaterial é obrigatório")
    @Valid
    private CryptographicDTO cryptographicMaterial;

    @NotNull(message = "certificateChain é obrigatório")
    @Size(min = 2, message = "certificateChain deve conter ao menos 2 certificados (folha + raiz ICP-Brasil)")
    private List<@NotBlank(message = "Cada certificado da cadeia deve ser uma string base64 não vazia") String> certificateChain;

    @NotNull(message = "Timestamp de referência é obrigatório")
    @Min(value = 1751328000L, message = "Timestamp abaixo do mínimo permitido (01/07/2025)")
    @Max(value = 4102444800L, message = "Timestamp acima do máximo permitido (31/12/2099)")
    private Long referenceTimestamp;

    @NotNull(message = "Informe a estratégia de timestamp")
    private TimestampStrategy timestampStrategy;

    @NotBlank(message = "policyUri é obrigatória")
    @Pattern(
            regexp = "^https://fhir\\.saude\\.go\\.gov\\.br/r4/seguranca/ImplementationGuide/br\\.go\\.ses\\.seguranca\\|\\d+\\.\\d+\\.\\d+$",
            message = "policyUri deve seguir o formato {baseUri}|{major.minor.patch}"
    )
    private String policyUri;
}