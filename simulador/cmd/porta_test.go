package cmd

import (
	"net"
	"testing"
)

func TestPortaDisponivel(t *testing.T) {
	// Porta alta improvável de estar em uso
	if !portaDisponivel(19999) {
		t.Error("esperava porta 19999 disponível, mas foi detectada como ocupada")
	}
}

func TestPortaOcupada(t *testing.T) {
	// Ocupa uma porta e verifica que é detectada como em uso
	ln, err := net.Listen("tcp4", ":19998")
	if err != nil {
		t.Skip("não foi possível ocupar a porta para o teste")
	}
	defer ln.Close()

	if portaDisponivel(19998) {
		t.Error("esperava porta 19998 ocupada, mas foi detectada como disponível")
	}
}