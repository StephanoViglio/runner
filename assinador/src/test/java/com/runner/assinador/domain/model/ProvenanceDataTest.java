package com.runner.assinador.domain.model;

import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProvenanceDataTest {

    @Test
    @DisplayName("Deve construir com sucesso quando a lista de targets é válida")
    void provenanceData_deveConstruirComSucesso_quandoTargetsValidos() {
        ProvenanceData provenance = new ProvenanceData(List.of(EntityUtils.FULL_URL_PADRAO));

        assertThat(provenance.getTargets()).containsExactly(EntityUtils.FULL_URL_PADRAO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a lista de targets é nula")
    void provenanceData_deveLancarExcecao_quandoTargetsNulo() {
        assertThatThrownBy(() -> new ProvenanceData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("provenance deve ter ao menos um target");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a lista de targets está vazia")
    void provenanceData_deveLancarExcecao_quandoTargetsVazio() {
        assertThatThrownBy(() -> new ProvenanceData(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("provenance deve ter ao menos um target");
    }

    @Test
    @DisplayName("Deve preservar o estado interno quando a lista original é alterada após a construção")
    void provenanceData_deveSerImutavel_quandoListaOriginalAlterada() {
        List<String> mutavel = new ArrayList<>();
        mutavel.add(EntityUtils.FULL_URL_PADRAO);

        ProvenanceData provenance = new ProvenanceData(mutavel);
        mutavel.add(EntityUtils.FULL_URL_2);

        assertThat(provenance.getTargets()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar uma lista imutável quando getTargets() é chamado")
    void provenanceData_deveRetornarListaImutavel_quandoChamarGetTargets() {
        ProvenanceData provenance = EntityUtils.provenanceDataValida();

        assertThatThrownBy(() -> provenance.getTargets().add(EntityUtils.FULL_URL_2))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}