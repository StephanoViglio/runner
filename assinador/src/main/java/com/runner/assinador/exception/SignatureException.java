package com.runner.assinador.exception;

import com.runner.assinador.utils.OperationOutcomeCode;
import lombok.Getter;

@Getter
public class SignatureException extends RuntimeException {

    private final OperationOutcomeCode outcomeCode;
    private final String diagnostics;

    public SignatureException(OperationOutcomeCode outcomeCode, String diagnostics) {
        super(diagnostics != null ? diagnostics : outcomeCode.getDisplay());
        this.outcomeCode = outcomeCode;
        this.diagnostics = diagnostics;
    }

    public SignatureException(OperationOutcomeCode outcomeCode) {
        this(outcomeCode, null);
    }
}