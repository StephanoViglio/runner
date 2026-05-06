package com.runner.assinador.utils;

import lombok.Getter;

@Getter
public enum IssueSeverity {
    FATAL("fatal"),
    ERROR("error"),
    WARNING("warning"),
    INFORMATION("information");

    private final String fhirCode;

    IssueSeverity(String fhirCode) {
        this.fhirCode = fhirCode;
    }

}