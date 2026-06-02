package com.runner.assinador.presentation.shared.outcome;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OperationOutcome {
    private List<Issue> issues;
}