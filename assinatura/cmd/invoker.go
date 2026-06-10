package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"time"

	"github.com/StephanoViglio/runner/shared/jdk"
)

const defaultServerURL = "http://localhost:8080"
const defaultJarPath = "../assinador/target/assinador.jar"
const httpTimeout = 10 * time.Second

var httpClient = &http.Client{Timeout: httpTimeout}

func hubsaudeDir() string {
	home, err := os.UserHomeDir()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Erro ao obter diretório home: %v\n", err)
		os.Exit(ExitErroInesperado)
	}
	return filepath.Join(home, ".hubsaude")
}

func invocarHTTP(endpoint string, conteudo []byte) {
	url := defaultServerURL + endpoint
	resp, err := httpClient.Post(url, "application/json", bytes.NewBuffer(conteudo))
	if err != nil {
		fmt.Fprintf(os.Stderr, "Erro ao chamar o assinador: %v\n", err)
		fmt.Fprintf(os.Stderr, "Dica: verifique se o servidor está em execução em %s\n", defaultServerURL)
		os.Exit(ExitErroUso)
	}
	defer resp.Body.Close()

	corpo, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Erro ao ler resposta do assinador: %v\n", err)
		os.Exit(ExitErroInesperado)
	}

	if resp.StatusCode != http.StatusOK {
		fmt.Fprintf(os.Stderr, "Assinador retornou erro %d:\n%s\n", resp.StatusCode, string(corpo))
		os.Exit(ExitErroUso)
	}

	imprimirJSON(corpo)
}

func invocarCLI(subcomando string, arquivoJSON string, jarPath string) {
	java := jdk.CaminhoJava(hubsaudeDir())
	cmd := exec.Command(java, "-jar", jarPath, subcomando, "--input", arquivoJSON)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		fmt.Fprintf(os.Stderr, "Erro ao executar o assinador.jar: %v\n", err)
		os.Exit(ExitErroUso)
	}
}

func lerArquivoJSON(caminho string) []byte {
	conteudo, err := os.ReadFile(caminho)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Erro ao ler o arquivo '%s': %v\n", caminho, err)
		os.Exit(ExitErroUso)
	}
	if !json.Valid(conteudo) {
		fmt.Fprintf(os.Stderr, "Erro: o arquivo '%s' não contém um JSON válido\n", caminho)
		os.Exit(ExitErroUso)
	}
	return conteudo
}

func imprimirJSON(corpo []byte) {
	var resultado interface{}
	if err := json.Unmarshal(corpo, &resultado); err != nil {
		fmt.Println(string(corpo))
		return
	}
	saida, _ := json.MarshalIndent(resultado, "", "  ")
	fmt.Println(string(saida))
}