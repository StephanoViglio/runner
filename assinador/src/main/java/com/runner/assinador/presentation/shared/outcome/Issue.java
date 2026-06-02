package com.runner.assinador.presentation.shared.outcome;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Issue {
    private String severity;
    private String code;
    private CodeableConcept details;
    private String diagnostics;
    private List<String> location;
}