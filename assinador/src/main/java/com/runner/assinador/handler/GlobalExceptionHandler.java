package com.runner.assinador.handler;

import com.runner.assinador.dto.outcome.OperationOutcomeDTO;
import com.runner.assinador.exception.SignatureException;
import com.runner.assinador.factory.OperationOutcomeFactory;
import com.runner.assinador.utils.IssueSeverity;
import com.runner.assinador.utils.OperationOutcomeCode;
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

    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<OperationOutcomeDTO> handleSignatureException(SignatureException ex) {
        log.warn("SignatureException: code={} diagnostics={}",
                ex.getOutcomeCode().getCode(), ex.getDiagnostics());

        OperationOutcomeDTO body = OperationOutcomeFactory.of(
                ex.getOutcomeCode(),
                ex.getDiagnostics());

        return ResponseEntity
                .status(resolveHttpStatus(ex.getOutcomeCode().getSeverity()))
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OperationOutcomeDTO> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("MethodArgumentNotValidException: {}", messages);

        return ResponseEntity
                .status(HTTP_UNPROCESSABLE_ENTITY)
                .body(OperationOutcomeFactory.ofBindingErrors(messages));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationOutcomeDTO> handleUnexpected(Exception ex) {
        log.error("Erro inesperado não tratado", ex);

        return ResponseEntity
                .status(HTTP_INTERNAL_SERVER_ERROR)
                .body(OperationOutcomeFactory.of(
                        OperationOutcomeCode.FORMAT_JSON_MALFORMED,
                        "Erro interno inesperado: " + ex.getMessage()));
    }

    private int resolveHttpStatus(IssueSeverity severity) {
        return switch (severity) {
            case FATAL, ERROR -> HTTP_UNPROCESSABLE_ENTITY;
            case WARNING, INFORMATION -> HttpStatus.OK.value();
        };
    }
}