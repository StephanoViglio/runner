package com.runner.assinador.factory;

import com.runner.assinador.dto.outcome.CodeableConceptDTO;
import com.runner.assinador.dto.outcome.CodingDTO;
import com.runner.assinador.dto.outcome.IssueDTO;
import com.runner.assinador.dto.outcome.OperationOutcomeDTO;
import com.runner.assinador.utils.OperationOutcomeCode;

import java.util.List;

public final class OperationOutcomeFactory {

    private OperationOutcomeFactory() {}

    public static OperationOutcomeDTO of(OperationOutcomeCode code, String diagnostics) {
        return OperationOutcomeDTO.builder()
                .issues(List.of(buildIssue(code, diagnostics)))
                .build();
    }

    public static OperationOutcomeDTO ofBindingErrors(List<String> fieldErrors) {
        List<IssueDTO> issues = fieldErrors.stream()
                .map(msg -> buildIssue(OperationOutcomeCode.FORMAT_BUNDLE_MALFORMED, msg))
                .toList();
        return OperationOutcomeDTO.builder()
                .issues(issues)
                .build();
    }

    private static IssueDTO buildIssue(OperationOutcomeCode code, String diagnostics) {
        CodingDTO coding = CodingDTO.builder()
                .system(OperationOutcomeCode.CODE_SYSTEM_URL)
                .code(code.getCode())
                .display(code.getDisplay())
                .build();

        CodeableConceptDTO details = CodeableConceptDTO.builder()
                .coding(List.of(coding))
                .text(code.getDisplay())
                .build();

        return IssueDTO.builder()
                .severity(code.getSeverity().getFhirCode())
                .code(code.getIssueType().getFhirCode())
                .details(details)
                .diagnostics(diagnostics)
                .build();
    }
}