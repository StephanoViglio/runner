package com.runner.assinador.utils;

import com.runner.assinador.presentation.in.rest.dto.request.BundleDTO;
import com.runner.assinador.presentation.in.rest.dto.request.CryptographicDTO;
import com.runner.assinador.presentation.in.rest.dto.request.ProvenanceDTO;
import com.runner.assinador.presentation.in.rest.dto.request.ResourceEntryDTO;
import com.runner.assinador.presentation.in.rest.dto.request.SignRequestDTO;
import com.runner.assinador.presentation.in.rest.dto.request.VerifyRequestDTO;

import java.time.Instant;
import java.util.List;

import static com.runner.assinador.domain.model.CryptographicStrategy.TOKEN;
import static com.runner.assinador.domain.model.TimestampStrategy.IAT;
import static com.runner.assinador.utils.EntityUtils.CERT_BASE64;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.POLICY_URI;
import static com.runner.assinador.utils.EntityUtils.RESOURCE_JSON;

public final class RestDTOUtils {

    public static final String SIGNATURE_DATA = "U0lHTkFUVVJFX0RBVEFfQkFTRTY0";

    private RestDTOUtils() {}

    public static ResourceEntryDTO resourceEntryDTOValido() {
        ResourceEntryDTO dto = new ResourceEntryDTO();
        dto.setFullUrl(FULL_URL_PADRAO);
        dto.setResourceJson(RESOURCE_JSON);
        return dto;
    }

    public static BundleDTO bundleDTOValido() {
        BundleDTO dto = new BundleDTO();
        dto.setEntry(List.of(resourceEntryDTOValido()));
        return dto;
    }

    public static ProvenanceDTO provenanceDTOValido() {
        ProvenanceDTO dto = new ProvenanceDTO();
        dto.setTarget(List.of(FULL_URL_PADRAO));
        return dto;
    }

    public static CryptographicDTO cryptographicDTOValido() {
        CryptographicDTO dto = new CryptographicDTO();
        dto.setCryptographicStrategy(TOKEN);
        dto.setPin("1234");
        dto.setIdentifier("cn=teste");
        dto.setSlotId(0);
        dto.setTokenLabel("TOKEN-LABEL");
        return dto;
    }

    public static SignRequestDTO signRequestDTOValido() {
        SignRequestDTO dto = new SignRequestDTO();
        dto.setBundle(bundleDTOValido());
        dto.setProvenance(provenanceDTOValido());
        dto.setCryptographicMaterial(cryptographicDTOValido());
        dto.setCertificateChain(List.of(CERT_BASE64));
        dto.setReferenceTimestamp(Instant.now().getEpochSecond());
        dto.setTimestampStrategy(IAT);
        dto.setPolicyUri(POLICY_URI);
        return dto;
    }

    public static VerifyRequestDTO verifyRequestDTOValidoComBundle() {
        VerifyRequestDTO dto = new VerifyRequestDTO();
        dto.setSignatureData(SIGNATURE_DATA);
        dto.setReferenceTimestamp(Instant.now().getEpochSecond());
        dto.setPolicyUri(POLICY_URI);
        dto.setBundle(bundleDTOValido());
        dto.setProvenance(provenanceDTOValido());
        return dto;
    }

    public static VerifyRequestDTO verifyRequestDTOValidoSemBundle() {
        VerifyRequestDTO dto = new VerifyRequestDTO();
        dto.setSignatureData(SIGNATURE_DATA);
        dto.setReferenceTimestamp(Instant.now().getEpochSecond());
        dto.setPolicyUri(POLICY_URI);
        return dto;
    }
}