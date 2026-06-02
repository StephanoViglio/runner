package com.runner.assinador.presentation.in.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CliOutputFormatter {

    private final ObjectMapper objectMapper;

    public CliOutputFormatter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void printSuccess(Object dto) {
        print(System.out, dto);
    }

    public void printError(Object dto) {
        print(System.err, dto);
    }

    private void print(java.io.PrintStream stream, Object dto) {
        try {
            String json = objectMapper
                    .writer()
                    .with(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(dto);
            stream.println(json);
        } catch (Exception e) {
            System.err.println("Erro ao serializar saída: " + e.getMessage());
        }
    }
}