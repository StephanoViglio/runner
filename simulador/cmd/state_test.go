package cmd

import (
	"os"
	"testing"
)

func TestSalvarELerState(t *testing.T) {
	// Usa diretório temporário para não poluir ~/.hubsaude
	dir := t.TempDir()
	hubsaudeDirOverride = dir

	state := &SimuladorState{
		PID:     12345,
		Port:    8443,
		Version: "1.0.0",
	}

	// Salva
	if err := salvarState(state); err != nil {
		t.Fatalf("erro ao salvar state: %v", err)
	}

	// Lê
	lido, err := lerState()
	if err != nil {
		t.Fatalf("erro ao ler state: %v", err)
	}

	if lido.PID != state.PID {
		t.Errorf("PID: esperava %d, recebi %d", state.PID, lido.PID)
	}
	if lido.Port != state.Port {
		t.Errorf("Port: esperava %d, recebi %d", state.Port, lido.Port)
	}
	if lido.Version != state.Version {
		t.Errorf("Version: esperava %s, recebi %s", state.Version, lido.Version)
	}
}

func TestLimparState(t *testing.T) {
	dir := t.TempDir()
	hubsaudeDirOverride = dir

	state := &SimuladorState{PID: 1, Port: 8443, Version: "1.0.0"}
	salvarState(state)

	if err := limparState(); err != nil {
		t.Fatalf("erro ao limpar state: %v", err)
	}

	if _, err := lerState(); err == nil {
		t.Error("esperava erro ao ler state após limpar, mas não houve")
	}
}

func TestLerStateInexistente(t *testing.T) {
	dir := t.TempDir()
	hubsaudeDirOverride = dir

	_, err := lerState()
	if err == nil {
		t.Error("esperava erro ao ler state inexistente, mas não houve")
	}
}

// Restaura ao final de cada teste
func init() {
	os.Setenv("HUBSAUDE_TEST", "1")
}