package com.runner.assinador.utils;

import lombok.Getter;

@Getter
public enum OperationOutcomeCode {

    POLICY_URI_INVALID(
            "POLICY.URI-INVALID",
            "URI da Política Inválida",
            IssueSeverity.ERROR,
            IssueType.VALUE),

    POLICY_VERSION_UNSUPPORTED(
            "POLICY.VERSION-UNSUPPORTED",
            "Versão da Política Não Suportada",
            IssueSeverity.ERROR,
            IssueType.NOT_SUPPORTED),

    FORMAT_BASE64_INVALID(
            "FORMAT.BASE64-INVALID",
            "Dados Base64 Inválidos",
            IssueSeverity.ERROR,
            IssueType.INVALID),

    FORMAT_BUNDLE_MALFORMED(
            "FORMAT.BUNDLE-MALFORMED",
            "Bundle FHIR Malformado",
            IssueSeverity.ERROR,
            IssueType.INVALID),

    FORMAT_DUPLICATE_FULLURL(
            "FORMAT.DUPLICATE-FULLURL",
            "fullUrl Duplicado no Bundle",
            IssueSeverity.ERROR,
            IssueType.DUPLICATE),

    FORMAT_PROVENANCE_TARGET_DUPLICATE(
            "FORMAT.PROVENANCE-TARGET-DUPLICATE",
            "Referência Duplicada em Provenance.target",
            IssueSeverity.ERROR,
            IssueType.DUPLICATE),

    FORMAT_TARGET_REFERENCE_MISSING(
            "FORMAT.TARGET-REFERENCE-MISSING",
            "Referência de Provenance.target Não Encontrada no Bundle",
            IssueSeverity.ERROR,
            IssueType.NOT_FOUND),

    FORMAT_BUNDLE_RESOURCE_MISSING(
            "FORMAT.BUNDLE-RESOURCE-MISSING",
            "Bundle.entry.resource Ausente para Referência Declarada",
            IssueSeverity.ERROR,
            IssueType.REQUIRED),

    FORMAT_JSON_MALFORMED(
            "FORMAT.JSON-MALFORMED",
            "JSON Malformado",
            IssueSeverity.ERROR,
            IssueType.INVALID),

    CONFIG_TSA_CONFIG_MISSING(
            "CONFIG.TSA-CONFIG-MISSING",
            "Configuração da TSA Ausente",
            IssueSeverity.ERROR,
            IssueType.REQUIRED),

    TIMESTAMP_OUT_OF_TOLERANCE_WINDOW(
            "TIMESTAMP.OUT-OF-TOLERANCE-WINDOW",
            "Timestamp Fora da Janela de Tolerância de ±5 Minutos",
            IssueSeverity.ERROR,
            IssueType.BUSINESS_RULE);


    public static final String CODE_SYSTEM_URL =
            "https://fhir.saude.go.gov.br/r4/seguranca/CodeSystem/situacao-excepcional-assinatura";

    private final String code;
    private final String display;
    private final IssueSeverity severity;
    private final IssueType issueType;

    OperationOutcomeCode(String code, String display,
                         IssueSeverity severity, IssueType issueType) {
        this.code      = code;
        this.display   = display;
        this.severity  = severity;
        this.issueType = issueType;
    }
}