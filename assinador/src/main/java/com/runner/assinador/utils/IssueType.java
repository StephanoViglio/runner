package com.runner.assinador.utils;

import lombok.Getter;

@Getter
public enum IssueType {
    INVALID("invalid"),
    REQUIRED("required"),
    VALUE("value"),
    SECURITY("security"),
    NOT_SUPPORTED("not-supported"),
    DUPLICATE("duplicate"),
    NOT_FOUND("not-found"),
    TOO_LONG("too-long"),
    BUSINESS_RULE("business-rule"),
    EXCEPTION("exception"),
    TIMEOUT("timeout"),
    INFORMATIONAL("informational");

    private final String fhirCode;

    IssueType(String fhirCode) {
        this.fhirCode = fhirCode;
    }
}