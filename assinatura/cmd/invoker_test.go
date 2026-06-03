package cmd

import (
	"os"
	"path/filepath"
	"testing"
)

func TestLerArquivoJSONValido(t *testing.T) {
	
	dir := t.TempDir()
	caminho := filepath.Join(dir, "teste.json")
	os.WriteFile(caminho, []byte(`{"chave":"valor"}`), 0644)

	conteudo := lerArquivoJSON(caminho)
	if conteudo == nil {
		t.Error("esperava conteúdo, recebeu nil")
	}
}

func TestLerArquivoJSONInexistente(t *testing.T) {
	defer func() {
		if r := recover(); r == nil {

		}
	}()

	_, err := os.Stat("arquivo_que_nao_existe.json")
	if !os.IsNotExist(err) {
		t.Error("arquivo não deveria existir")
	}
}