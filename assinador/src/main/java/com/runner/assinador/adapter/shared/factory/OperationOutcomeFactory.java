package com.runner.assinador.adapter.shared.factory;

import com.runner.assinador.adapter.shared.outcome.CodeableConcept;
import com.runner.assinador.adapter.shared.outcome.Coding;
import com.runner.assinador.adapter.shared.outcome.Issue;
import com.runner.assinador.adapter.shared.outcome.OperationOutcome;
import com.runner.assinador.adapter.shared.OperationOutcomeCode;
import com.runner.assinador.domain.model.VerificationResult;

import java.util.List;

public final class OperationOutcomeFactory {

    private OperationOutcomeFactory() {}

    public static OperationOutcome of(OperationOutcomeCode code, String diagnostics) {
        return OperationOutcome.builder()
                .issues(List.of(buildIssue(code, diagnostics)))
                .build();
    }

    public static OperationOutcome fromVerificationResult(VerificationResult result) {
        OperationOutcomeCode code = result.isSuccess()
                ? OperationOutcomeCode.VALIDATION_SUCCESS
                : OperationOutcomeCode.fromCode(result.getCode());

        return of(code, result.getMessage());
    }

    public static OperationOutcome ofBindingErrors(List<String> fieldErrors) {
        List<Issue> issues = fieldErrors.stream()
                .map(msg -> buildIssue(OperationOutcomeCode.FORMAT_BUNDLE_MALFORMED, msg))
                .toList();
        return OperationOutcome.builder().issues(issues).build();
    }

    private static Issue buildIssue(OperationOutcomeCode code, String diagnostics) {
        Coding coding = Coding.builder()
                .system(OperationOutcomeCode.CODE_SYSTEM_URL)
                .code(code.getCode())
                .display(code.getDisplay())
                .build();

        CodeableConcept details = CodeableConcept.builder()
                .coding(List.of(coding))
                .text(code.getDisplay())
                .build();

        return Issue.builder()
                .severity(code.getSeverity().getFhirCode())
                .code(code.getIssueType().getFhirCode())
                .details(details)
                .diagnostics(diagnostics)
                .build();
    }
}