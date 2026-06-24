package com.runner.assinador.presentation.shared.signature;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public final class SignedContentDigest {

    private SignedContentDigest() {}

    public static String computeBase64Url(BundleData bundle, ProvenanceData provenance) {
        Map<String, ResourceEntry> entryByFullUrl = bundle.getEntries().stream()
                .collect(Collectors.toMap(ResourceEntry::getFullUrl, e -> e));

        StringBuilder concatenated = new StringBuilder();
        for (String ref : provenance.getTargets()) {
            ResourceEntry entry = entryByFullUrl.get(ref);
            if (entry == null) {
                throw new SignatureException(
                        DomainErrorCode.FORMAT_TARGET_REFERENCE_MISSING,
                        "provenance.target referencia '" + ref +
                                "', mas nenhuma entry no bundle possui esse fullUrl.");
            }
            concatenated.append(entry.getResourceJson());
        }

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sha256(concatenated.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(
                    DomainErrorCode.CRYPTO_DIGEST_FAILURE,
                    "Falha ao calcular SHA-256: " + e.getMessage());
        }
    }
}
