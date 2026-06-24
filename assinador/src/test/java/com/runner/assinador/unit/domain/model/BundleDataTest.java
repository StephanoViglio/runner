package com.runner.assinador.unit.domain.model;

import com.runner.assinador.domain.model.BundleData;
import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BundleDataTest {

    @Test
    @DisplayName("Deve construir com sucesso quando recebe uma lista de entries válida")
    void bundleData_deveConstruirComSucesso_quandoEntriesValidas() {
        ResourceEntry entry = EntityUtils.resourceEntryValida();

        BundleData bundle = new BundleData(List.of(entry));

        assertThat(bundle.getEntries()).containsExactly(entry);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a lista de entries é nula")
    void bundleData_deveLancarExcecao_quandoEntriesNula() {
        assertThatThrownBy(() -> new BundleData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle deve ter ao menos uma entry");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a lista de entries está vazia")
    void bundleData_deveLancarExcecao_quandoEntriesVazia() {
        assertThatThrownBy(() -> new BundleData(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bundle deve ter ao menos uma entry");
    }

    @Test
    @DisplayName("Deve preservar o estado interno quando a lista original é alterada após a construção")
    void bundleData_deveSerImutavel_quandoListaOriginalAlterada() {
        List<ResourceEntry> mutavel = new ArrayList<>();
        mutavel.add(EntityUtils.resourceEntryValida());

        BundleData bundle = new BundleData(mutavel);
        mutavel.add(EntityUtils.resourceEntryValida(EntityUtils.FULL_URL_2));

        assertThat(bundle.getEntries()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar uma lista imutável quando getEntries() é chamado")
    void bundleData_deveRetornarListaImutavel_quandoChamarGetEntries() {
        BundleData bundle = EntityUtils.bundleDataValido();

        assertThatThrownBy(() -> bundle.getEntries().add(EntityUtils.resourceEntryValida()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}