package com.runner.assinador.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BundleDTO {

    @NotEmpty(message = "Informe ao menos uma entry para o bundle")
    @Valid
    private List<ResourceEntryDTO> entry;

}