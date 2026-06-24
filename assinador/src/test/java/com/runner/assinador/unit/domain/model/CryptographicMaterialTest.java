package com.runner.assinador.unit.domain.model;

import com.runner.assinador.domain.model.CryptographicMaterial;
import com.runner.assinador.domain.model.CryptographicStrategy;
import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptographicMaterialTest {

    @Test
    @DisplayName("Deve construir com sucesso quando todos os campos obrigatórios são válidos")
    void cryptographicMaterial_deveConstruirComSucesso_quandoTodosCamposValidos() {
        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.TOKEN,
                "1234",
                "cn=teste",
                0,
                "LABEL",
                List.of(EntityUtils.CERT_BASE64)
        );

        assertThat(material.getStrategy()).isEqualTo(CryptographicStrategy.TOKEN);
        assertThat(material.getPin()).isEqualTo("1234");
        assertThat(material.getIdentifier()).isEqualTo("cn=teste");
        assertThat(material.getSlotId()).isZero();
        assertThat(material.getTokenLabel()).isEqualTo("LABEL");
        assertThat(material.getCertificateChain()).containsExactly(EntityUtils.CERT_BASE64);
    }

    @Test
    @DisplayName("Deve aceitar slotId e tokenLabel nulos (campos opcionais para SMARTCARD)")
    void cryptographicMaterial_deveConstruirComSucesso_quandoSlotIdENulo() {
        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.SMARTCARD,
                "1234",
                "cn=teste",
                null,
                null,
                List.of(EntityUtils.CERT_BASE64)
        );

        assertThat(material.getSlotId()).isNull();
        assertThat(material.getTokenLabel()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando strategy é nula")
    void cryptographicMaterial_deveLancarExcecao_quandoStrategyNula() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                null, "1234", "cn=teste", 0, "LABEL", List.of(EntityUtils.CERT_BASE64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cryptographicStrategy é obrigatória");
    }

    @Test
    @DisplayName("Deve lançar exceção quando pin é nulo")
    void cryptographicMaterial_deveLancarExcecao_quandoPinNulo() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, null, "cn=teste", 0, "LABEL", List.of(EntityUtils.CERT_BASE64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pin é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando pin contém apenas espaços em branco")
    void cryptographicMaterial_deveLancarExcecao_quandoPinEmBranco() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "   ", "cn=teste", 0, "LABEL", List.of(EntityUtils.CERT_BASE64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pin é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando identifier é nulo")
    void cryptographicMaterial_deveLancarExcecao_quandoIdentifierNulo() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", null, 0, "LABEL", List.of(EntityUtils.CERT_BASE64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("identifier é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando identifier contém apenas espaços em branco")
    void cryptographicMaterial_deveLancarExcecao_quandoIdentifierEmBranco() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "  ", 0, "LABEL", List.of(EntityUtils.CERT_BASE64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("identifier é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando certificateChain é nula")
    void cryptographicMaterial_deveLancarExcecao_quandoCertificateChainNula() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "cn=teste", 0, "LABEL", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("certificateChain deve ter ao menos um certificado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando certificateChain está vazia")
    void cryptographicMaterial_deveLancarExcecao_quandoCertificateChainVazia() {
        assertThatThrownBy(() -> new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "cn=teste", 0, "LABEL", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("certificateChain deve ter ao menos um certificado");
    }

    @Test
    @DisplayName("Deve preservar a cadeia de certificados quando a lista original é alterada")
    void cryptographicMaterial_deveSerImutavel_quandoListaOriginalDeCertificadosAlterada() {
        List<String> mutavel = new ArrayList<>();
        mutavel.add(EntityUtils.CERT_BASE64);

        CryptographicMaterial material = new CryptographicMaterial(
                CryptographicStrategy.TOKEN, "1234", "cn=teste", 0, "LABEL", mutavel);
        mutavel.add("OUTRO_CERTIFICADO");

        assertThat(material.getCertificateChain()).hasSize(1);
    }
}