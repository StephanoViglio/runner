package com.runner.assinador.adapter.in.rest;

import com.runner.assinador.adapter.in.rest.dto.response.OperationOutcomeDTO;
import com.runner.assinador.adapter.shared.IssueSeverity;
import com.runner.assinador.adapter.shared.OperationOutcomeCode;
import com.runner.assinador.adapter.shared.factory.OperationOutcomeFactory;
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
    public ResponseEntity<OperationOutcomeDTO> handleSignatureException(SignatureException ex) {
        OperationOutcomeCode code = OperationOutcomeCode.fromCode(ex.getErrorCode().getCode());

        log.warn("SignatureException: code={} diagnostics={}", code.getCode(), ex.getDiagnostics());

        return ResponseEntity
                .status(resolveHttpStatus(code.getSeverity()))
                .body(OperationOutcomeFactory.of(code, ex.getDiagnostics()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OperationOutcomeDTO> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("MethodArgumentNotValidException: {}", messages);

        return ResponseEntity
                .status(422)
                .body(OperationOutcomeFactory.ofBindingErrors(messages));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationOutcomeDTO> handleUnexpected(Exception ex) {
        log.error("Erro inesperado não tratado", ex);

        return ResponseEntity
                .status(500)
                .body(OperationOutcomeFactory.of(
                        OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                        "Erro interno inesperado: " + ex.getMessage()));
    }

    private int resolveHttpStatus(IssueSeverity severity) {
        return switch (severity) {
            case FATAL, ERROR -> 422;
            case WARNING, INFORMATION -> HttpStatus.OK.value();
        };
    }
}