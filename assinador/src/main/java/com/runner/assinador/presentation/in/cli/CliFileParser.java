package com.runner.assinador.presentation.in.cli;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class CliFileParser {

    private final ObjectMapper objectMapper;

    public CliFileParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T parse(File file, Class<T> type) {
        if (!file.exists()) {
            System.err.println("Arquivo não encontrado: " + file.getAbsolutePath());
            System.exit(1);
        }
        try {
            return objectMapper.readValue(file, type);
        } catch (Exception e) {
            System.err.println("Erro ao ler arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}