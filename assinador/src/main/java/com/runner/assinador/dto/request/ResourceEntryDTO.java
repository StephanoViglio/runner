package com.runner.assinador.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResourceEntryDTO {

    private String resourceJson;

    private String uuid;
}