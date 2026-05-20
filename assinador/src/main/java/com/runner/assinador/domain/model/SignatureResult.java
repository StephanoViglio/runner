package com.runner.assinador.domain.model;

import java.util.List;

public class SignatureResult {

    private final List<SignatureCoding> type;
    private final String when;
    private final String signerCpf;
    private final String targetFormat;
    private final String sigFormat;
    private final String data;

    public SignatureResult(List<SignatureCoding> type, String when, String signerCpf, String targetFormat,
                           String sigFormat, String data) {

        this.type = List.copyOf(type);
        this.when = when;
        this.signerCpf = signerCpf;
        this.targetFormat = targetFormat;
        this.sigFormat = sigFormat;
        this.data = data;
    }

    public List<SignatureCoding> getType() { return type; }
    public String getWhen() { return when; }
    public String getSignerCpf() { return signerCpf; }
    public String getTargetFormat() { return targetFormat; }
    public String getSigFormat() { return sigFormat; }
    public String getData() { return data; }

    public record SignatureCoding(String system, String code) {}
}