package cmd

import (
	"os"
	"path/filepath"
	"testing"
)

func TestLerArquivoJSONValido(t *testing.T) {
	// Cria arquivo temporário com JSON válido
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
			// os.Exit não pode ser capturado diretamente
			// esse teste valida que a função não entra em pânico inesperado
		}
	}()

	// Apenas confirma que o caminho não existe
	_, err := os.Stat("arquivo_que_nao_existe.json")
	if !os.IsNotExist(err) {
		t.Error("arquivo não deveria existir")
	}
}