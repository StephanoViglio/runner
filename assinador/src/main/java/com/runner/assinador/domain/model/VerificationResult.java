package com.runner.assinador.domain.model;

public class VerificationResult {

    public enum Status { SUCCESS, FAILURE }

    private final Status status;
    private final String code;
    private final String message;

    private VerificationResult(Status status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static VerificationResult success(String message) {
        return new VerificationResult(Status.SUCCESS, "VALIDATION.SUCCESS", message);
    }

    public static VerificationResult failure(String code, String message) {
        return new VerificationResult(Status.FAILURE, code, message);
    }

    public Status getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
}