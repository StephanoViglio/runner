package com.runner.assinador.adapter.shared.factory;

import com.runner.assinador.adapter.in.rest.dto.response.CodeableConceptDTO;
import com.runner.assinador.adapter.in.rest.dto.response.CodingDTO;
import com.runner.assinador.adapter.in.rest.dto.response.IssueDTO;
import com.runner.assinador.adapter.in.rest.dto.response.OperationOutcomeDTO;
import com.runner.assinador.adapter.shared.OperationOutcomeCode;
import com.runner.assinador.domain.model.VerificationResult;

import java.util.List;

public final class OperationOutcomeFactory {

    private OperationOutcomeFactory() {}

    public static OperationOutcomeDTO of(OperationOutcomeCode code, String diagnostics) {
        return OperationOutcomeDTO.builder()
                .issues(List.of(buildIssue(code, diagnostics)))
                .build();
    }

    public static OperationOutcomeDTO fromVerificationResult(VerificationResult result) {
        OperationOutcomeCode code = result.isSuccess()
                ? OperationOutcomeCode.VALIDATION_SUCCESS
                : OperationOutcomeCode.fromCode(result.getCode());

        return of(code, result.getMessage());
    }

    public static OperationOutcomeDTO ofBindingErrors(List<String> fieldErrors) {
        List<IssueDTO> issues = fieldErrors.stream()
                .map(msg -> buildIssue(OperationOutcomeCode.FORMAT_BUNDLE_MALFORMED, msg))
                .toList();
        return OperationOutcomeDTO.builder().issues(issues).build();
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