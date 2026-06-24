package com.runner.assinador.unit.presentation.shared.signature;

import com.runner.assinador.domain.exception.DomainErrorCode;
import com.runner.assinador.domain.exception.SignatureException;
import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ProvenanceData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.presentation.shared.signature.SignedContentDigest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.runner.assinador.utils.EntityUtils.FULL_URL_2;
import static com.runner.assinador.utils.EntityUtils.FULL_URL_PADRAO;
import static com.runner.assinador.utils.EntityUtils.bundleDataValido;
import static com.runner.assinador.utils.EntityUtils.provenanceDataValida;
import static com.runner.assinador.utils.EntityUtils.resourceEntryValida;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignedContentDigestTest {

    @Test
    @DisplayName("computeBase64Url deve ser determinístico para o mesmo bundle e provenance")
    void computeBase64Url_deveSerDeterministico() {
        BundleData bundle = bundleDataValido();
        ProvenanceData provenance = provenanceDataValida();

        String hash1 = SignedContentDigest.computeBase64Url(bundle, provenance);
        String hash2 = SignedContentDigest.computeBase64Url(bundle, provenance);

        assertThat(hash1).isEqualTo(hash2).isNotBlank();
    }

    @Test
    @DisplayName("computeBase64Url deve lançar CRYPTO... FORMAT_TARGET_REFERENCE_MISSING quando target não existe no bundle")
    void computeBase64Url_deveLancarExcecao_quandoTargetNaoExisteNoBundle() {
        BundleData bundle = new BundleData(List.of(resourceEntryValida(FULL_URL_2)));
        ProvenanceData provenance = provenanceDataValida();

        assertThatThrownBy(() -> SignedContentDigest.computeBase64Url(bundle, provenance))
                .isInstanceOf(SignatureException.class)
                .matches(ex -> ((SignatureException) ex).getErrorCode()
                        == DomainErrorCode.FORMAT_TARGET_REFERENCE_MISSING);
    }

    @Test
    @DisplayName("computeBase64Url deve produzir hashes diferentes para conteúdos diferentes")
    void computeBase64Url_deveProduzirHashesDiferentes_paraConteudosDiferentes() {
        BundleData bundleA = bundleDataValido();
        BundleData bundleB = new BundleData(List.of(
                new ResourceEntry(FULL_URL_PADRAO, "{\"resourceType\":\"Patient\",\"id\":\"outro-paciente\"}")
        ));

        String hashA = SignedContentDigest.computeBase64Url(bundleA, provenanceDataValida());
        String hashB = SignedContentDigest.computeBase64Url(bundleB, provenanceDataValida());

        assertThat(hashA).isNotEqualTo(hashB);
    }
}
