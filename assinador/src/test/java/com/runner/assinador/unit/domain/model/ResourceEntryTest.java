package com.runner.assinador.unit.domain.model;

import com.runner.assinador.domain.model.ResourceEntry;
import com.runner.assinador.utils.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceEntryTest {

    @Test
    @DisplayName("Deve construir com sucesso quando fullUrl e resourceJson são válidos")
    void resourceEntry_deveConstruirComSucesso_quandoFullUrlEResourceJsonValidos() {
        ResourceEntry entry = new ResourceEntry(EntityUtils.FULL_URL_PADRAO, EntityUtils.RESOURCE_JSON);

        assertThat(entry.getFullUrl()).isEqualTo(EntityUtils.FULL_URL_PADRAO);
        assertThat(entry.getResourceJson()).isEqualTo(EntityUtils.RESOURCE_JSON);
    }

    @Test
    @DisplayName("Deve lançar exceção quando fullUrl é nulo")
    void resourceEntry_deveLancarExcecao_quandoFullUrlNulo() {
        assertThatThrownBy(() -> new ResourceEntry(null, EntityUtils.RESOURCE_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fullUrl é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando fullUrl é string vazia")
    void resourceEntry_deveLancarExcecao_quandoFullUrlVazio() {
        assertThatThrownBy(() -> new ResourceEntry("", EntityUtils.RESOURCE_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fullUrl é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando fullUrl contém apenas espaços em branco")
    void resourceEntry_deveLancarExcecao_quandoFullUrlEmBranco() {
        assertThatThrownBy(() -> new ResourceEntry("   ", EntityUtils.RESOURCE_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fullUrl é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando fullUrl não segue o formato urn:uuid:<UUID>")
    void resourceEntry_deveLancarExcecao_quandoFullUrlNaoSegueFormatoUrnUuid() {
        assertThatThrownBy(() -> new ResourceEntry("urn:isso-nao-eh-uuid", EntityUtils.RESOURCE_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fullUrl deve seguir o formato urn:uuid:<UUID RFC 4122>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o UUID dentro do fullUrl está malformado")
    void resourceEntry_deveLancarExcecao_quandoFullUrlPossuiUuidMalformado() {
        assertThatThrownBy(() -> new ResourceEntry("urn:uuid:nao-eh-uuid-valido", EntityUtils.RESOURCE_JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fullUrl deve seguir o formato urn:uuid:<UUID RFC 4122>");
    }

    @Test
    @DisplayName("Deve lançar exceção quando resourceJson é nulo")
    void resourceEntry_deveLancarExcecao_quandoResourceJsonNulo() {
        assertThatThrownBy(() -> new ResourceEntry(EntityUtils.FULL_URL_PADRAO, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resourceJson é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando resourceJson contém apenas espaços em branco")
    void resourceEntry_deveLancarExcecao_quandoResourceJsonEmBranco() {
        assertThatThrownBy(() -> new ResourceEntry(EntityUtils.FULL_URL_PADRAO, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resourceJson é obrigatório");
    }
}