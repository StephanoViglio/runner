package com.runner.assinador.presentation.in.rest;

import com.runner.assinador.presentation.shared.outcome.OperationOutcome;
import com.runner.assinador.presentation.shared.IssueSeverity;
import com.runner.assinador.presentation.shared.OperationOutcomeCode;
import com.runner.assinador.presentation.shared.factory.OperationOutcomeFactory;
import com.runner.assinador.domain.exception.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<OperationOutcome> handleSignatureException(SignatureException ex) {
        OperationOutcomeCode code = OperationOutcomeCode.fromCode(ex.getErrorCode().getCode());

        log.warn("SignatureException: code={} diagnostics={}", code.getCode(), ex.getDiagnostics());

        return ResponseEntity
                .status(resolveHttpStatus(code.getSeverity()))
                .body(OperationOutcomeFactory.of(code, ex.getDiagnostics()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OperationOutcome> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("MethodArgumentNotValidException: {}", messages);

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(OperationOutcomeFactory.ofBindingErrors(messages));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationOutcome> handleUnexpected(Exception ex) {
        log.error("Erro inesperado não tratado", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationOutcomeFactory.of(
                        OperationOutcomeCode.INTERNAL_SERVER_ERROR,
                        "Erro interno inesperado. Consulte os logs do servidor."));
    }

    private int resolveHttpStatus(IssueSeverity severity) {
        return switch (severity) {
            case FATAL -> HttpStatus.INTERNAL_SERVER_ERROR.value();
            case ERROR -> HttpStatus.UNPROCESSABLE_ENTITY.value();
            case WARNING, INFORMATION -> HttpStatus.OK.value();
        };
    }
}