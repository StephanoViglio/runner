package com.runner.assinador.dto.outcome;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OperationOutcomeDTO {
    private List<IssueDTO> issues;
}