package com.runner.assinador.domain.exception;

public class SignatureException extends RuntimeException {

    private final DomainErrorCode errorCode;
    private final String diagnostics;

    public SignatureException(DomainErrorCode errorCode, String diagnostics) {
        super(diagnostics);
        this.errorCode = errorCode;
        this.diagnostics = diagnostics;
    }

    public SignatureException(DomainErrorCode errorCode) {
        this(errorCode, null);
    }

    public DomainErrorCode getErrorCode() { return errorCode; }
    public String getDiagnostics() { return diagnostics; }
}