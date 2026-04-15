package com.runner.assinador.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Informe ao menos uma resource para o bundle")
    private List<ResourceEntryDTO> resources;

}