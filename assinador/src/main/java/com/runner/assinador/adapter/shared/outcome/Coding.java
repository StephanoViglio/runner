package com.runner.assinador.adapter.shared.outcome;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Coding {
    private String system;
    private String code;
    private String display;
}