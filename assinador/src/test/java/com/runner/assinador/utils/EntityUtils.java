package com.runner.assinador.utils;

import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.CryptographicMaterial;
import com.runner.assinador.domain.model.CryptographicStrategy;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.domain.model.SignatureRequest;
import com.runner.assinador.domain.model.SignatureResult;
import com.runner.assinador.domain.model.TimestampStrategy;
import com.runner.assinador.domain.model.VerificationRequest;
import com.runner.assinador.domain.port.in.SignDocumentCommand;
import com.runner.assinador.domain.port.in.VerifySignatureCommand;

import java.time.Instant;
import java.util.List;

public final class EntityUtils {

    public static final String FULL_URL_PADRAO  = "urn:uuid:11111111-1111-4111-8111-111111111111";
    public static final String FULL_URL_2 = "urn:uuid:22222222-2222-4222-8222-222222222222";
    public static final String RESOURCE_JSON = "{\"resourceType\":\"Patient\",\"id\":\"p1\"}";
    public static final String POLICY_URI = "https://fhir.saude.go.gov.br/r4/seguranca/policy|1.0.0";
    public static final String CERT_BASE64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAfake";

    private EntityUtils() {}

    public static ResourceEntry resourceEntryValida() {
        return new ResourceEntry(FULL_URL_PADRAO, RESOURCE_JSON);
    }

    public static ResourceEntry resourceEntryValida(String fullUrl) {
        return new ResourceEntry(fullUrl, RESOURCE_JSON);
    }

    public static BundleData bundleDataValido() {
        return new BundleData(List.of(resourceEntryValida()));
    }

    public static ProvenanceData provenanceDataValida() {
        return new ProvenanceData(List.of(FULL_URL_PADRAO));
    }

    public static CryptographicMaterial cryptographicMaterialValido() {
        return new CryptographicMaterial(
                CryptographicStrategy.TOKEN,
                "1234",
                "cn=teste",
                0,
                "TOKEN-LABEL",
                List.of(CERT_BASE64)
        );
    }

    public static long timestampValido() {
        return Instant.now().getEpochSecond();
    }

    public static SignDocumentCommand signDocumentCommandValido() {
        return new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                timestampValido(),
                TimestampStrategy.IAT,
                POLICY_URI
        );
    }

    public static VerifySignatureCommand verifySignatureCommandValido() {
        return new VerifySignatureCommand(
                "U0lHTkFUVVJFX0RBVEFfQkFTRTY0",
                timestampValido(),
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );
    }

    public static SignatureRequest signatureRequestValido() {
        return new SignatureRequest(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                timestampValido(),
                TimestampStrategy.IAT,
                POLICY_URI
        );
    }

    public static VerificationRequest verificationRequestValido() {
        return new VerificationRequest(
                "U0lHTkFUVVJFX0RBVEFfQkFTRTY0",
                timestampValido(),
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );
    }

    public static SignatureResult signatureResultValido() {
        return new SignatureResult(
                List.of(new SignatureResult.SignatureCoding("urn:iso-astm:E1762-95:2013", "1.2.840.10065.1.12.1.1")),
                Instant.now().toString(),
                "12345678900",
                "application/octet-stream",
                "application/jose",
                "FAKE_SIGNATURE_DATA"
        );
    }

    public static SignDocumentCommand signDocumentCommandValido(long referenceTimestamp) {
        return new SignDocumentCommand(
                bundleDataValido(),
                provenanceDataValida(),
                cryptographicMaterialValido(),
                referenceTimestamp,
                TimestampStrategy.IAT,
                POLICY_URI
        );
    }

    public static SignDocumentCommand signDocumentCommandValido(BundleData bundle) {
        return new SignDocumentCommand(
                bundle,
                provenanceDataValida(),
                cryptographicMaterialValido(),
                Instant.now().getEpochSecond(),
                TimestampStrategy.IAT,
                POLICY_URI
        );
    }

    public static SignDocumentCommand signDocumentCommandValido(BundleData bundle, ProvenanceData provenance) {
        return new SignDocumentCommand(
                bundle,
                provenance,
                cryptographicMaterialValido(),
                Instant.now().getEpochSecond(),
                TimestampStrategy.IAT,
                POLICY_URI
        );
    }

    public static VerifySignatureCommand verifySignatureCommandValido(long referenceTimestamp) {
        return new VerifySignatureCommand(
                "U0lHTkFUVVJFX0RBVEFfQkFTRTY0",
                referenceTimestamp,
                POLICY_URI,
                bundleDataValido(),
                provenanceDataValida()
        );
    }

    public static VerifySignatureCommand verifySignatureCommandValidoSemBundle() {
        return new VerifySignatureCommand(
                "U0lHTkFUVVJFX0RBVEFfQkFTRTY0",
                Instant.now().getEpochSecond(),
                POLICY_URI,
                null,
                null
        );
    }

    public static VerificationRequest verificationRequestComSignatureData(String signatureData) {
        return new VerificationRequest(
                signatureData,
                timestampValido(),
                POLICY_URI,
                null,
                null
        );
    }
}