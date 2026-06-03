package cmd

import (
	"net"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestInstanciaVivaRespondendo(t *testing.T) {
	// Sobe um servidor fake que responde 200 no /api/info
	servidor := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	defer servidor.Close()

	// Extrai a porta do servidor fake
	porta := servidor.Listener.Addr().(*net.TCPAddr).Port
	if !instanciaViva(porta) {
		t.Error("esperava instância viva, mas foi detectada como inativa")
	}
}

func TestInstanciaVivaForaDar(t *testing.T) {
	// Porta improvável de ter algo respondendo
	if instanciaViva(19997) {
		t.Error("esperava instância inativa, mas foi detectada como viva")
	}
}
