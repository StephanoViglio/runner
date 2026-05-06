package com.runner.assinador.dto.outcome;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodingDTO {
    private String system;
    private String code;
    private String display;
}