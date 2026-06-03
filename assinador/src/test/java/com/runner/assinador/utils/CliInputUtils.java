package com.runner.assinador.utils;

import com.runner.assinador.presentation.in.cli.input.*;

import java.time.Instant;
import java.util.List;

import static com.runner.assinador.domain.model.CryptographicStrategy.TOKEN;
import static com.runner.assinador.domain.model.TimestampStrategy.IAT;
import static com.runner.assinador.utils.EntityUtils.CERT_BASE64;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.RESOURCE_JSON;

public final class CliInputUtils {

    public static final String SIGNATURE_DATA = "U0lHTkFUVVJFX0RBVEFfQkFTRTY0";

    private CliInputUtils() {}

    public static ResourceEntryInput resourceEntryInputValido() {
        return new ResourceEntryInput(FULL_URL_PADRAO, RESOURCE_JSON);
    }

    public static BundleInput bundleInputValido() {
        return new BundleInput(List.of(resourceEntryInputValido()));
    }

    public static ProvenanceInput provenanceInputValido() {
        return new ProvenanceInput(List.of(FULL_URL_PADRAO));
    }

    public static CryptographicInput cryptographicInputValido() {
        return new CryptographicInput(TOKEN, "1234", "cn=teste", 0, "TOKEN-LABEL");
    }

    public static SignInput signInputValido() {
        return new SignInput(
                bundleInputValido(),
                provenanceInputValido(),
                cryptographicInputValido(),
                List.of(CERT_BASE64),
                Instant.now().getEpochSecond(),
                IAT,
                POLICY_URI
        );
    }

    public static VerifyInput verifyInputValidoComBundle() {
        return new VerifyInput(
                SIGNATURE_DATA,
                Instant.now().getEpochSecond(),
                POLICY_URI,
                bundleInputValido(),
                provenanceInputValido()
        );
    }

    public static VerifyInput verifyInputValidoSemBundle() {
        return new VerifyInput(
                SIGNATURE_DATA,
                Instant.now().getEpochSecond(),
                POLICY_URI,
                null,
                null
        );
    }
}