package cmd

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"github.com/StephanoViglio/runner/shared/jdk"
)

const releaseJSONURL = "https://raw.githubusercontent.com/StephanoViglio/runner/main/release.json"

type ReleaseInfo struct {
	Jar struct {
		URL     string `json:"url"`
		Version string `json:"version"`
	} `json:"jar"`
}

func buscarReleaseInfo() (*ReleaseInfo, error) {
	resp, err := http.Get(releaseJSONURL)
	if err != nil {
		return nil, fmt.Errorf("erro ao buscar release.json: %v", err)
	}
	defer resp.Body.Close()

	var info ReleaseInfo
	if err := json.NewDecoder(resp.Body).Decode(&info); err != nil {
		return nil, fmt.Errorf("erro ao ler release.json: %v", err)
	}
	return &info, nil
}

func jarLocalPath() string {
	return filepath.Join(hubsaudeDir(), "simulador.jar")
}

func versaoLocalJar() string {
	state, err := lerState()
	if err != nil {
		return ""
	}
	return state.Version
}

func garantirJar(info *ReleaseInfo) error {
	versaoLocal := versaoLocalJar()

	if versaoLocal == info.Jar.Version {
		if _, err := os.Stat(jarLocalPath()); err == nil {
			fmt.Printf("simulador.jar já está na versão %s\n", versaoLocal)
			return nil
		}
	}

	fmt.Printf("Baixando simulador.jar versão %s...\n", info.Jar.Version)
	if err := jdk.BaixarArquivo(info.Jar.URL, jarLocalPath()); err != nil {
		return fmt.Errorf("erro ao baixar simulador.jar: %v", err)
	}
	fmt.Println("simulador.jar baixado com sucesso.")
	return nil
}

func garantirJRE() error {
	return jdk.GarantirJDK(hubsaudeDir())
}

func caminhoJava() string {
	return jdk.CaminhoJava(hubsaudeDir())
}