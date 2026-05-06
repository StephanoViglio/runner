package com.runner.assinador.dto.outcome;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueDTO {
    private String severity;
    private String code;
    private CodeableConceptDTO details;
    private String diagnostics;
    private List<String> location;
}