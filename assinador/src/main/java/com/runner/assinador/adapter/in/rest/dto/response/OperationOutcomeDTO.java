package com.runner.assinador.adapter.in.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OperationOutcomeDTO {
    private List<IssueDTO> issues;
}