package cmd

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
)

const releaseJSONURL = "https://raw.githubusercontent.com/StephanoViglio/runner/main/release.json"

type ReleaseInfo struct {
	Jar struct {
		URL     string `json:"url"`
		Version string `json:"version"`
	} `json:"jar"`
	JRE struct {
		WindowsX64 string `json:"windows_x64"`
		LinuxX64   string `json:"linux_x64"`
		MacX64     string `json:"mac_x64"`
	} `json:"jre"`
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

func jreDirPath() string {
	return filepath.Join(hubsaudeDir(), "jre")
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
	if err := baixarArquivo(info.Jar.URL, jarLocalPath()); err != nil {
		return fmt.Errorf("erro ao baixar simulador.jar: %v", err)
	}
	fmt.Println("simulador.jar baixado com sucesso.")
	return nil
}

func garantirJRE(info *ReleaseInfo) error {
	if _, err := os.Stat(jreDirPath()); err == nil {
		fmt.Println("JRE já disponível em ~/.hubsaude/jre")
		return nil
	}

	url := jreURLParaPlataforma(info)
	if url == "" {
		return fmt.Errorf("plataforma não suportada: %s/%s", runtime.GOOS, runtime.GOARCH)
	}

	fmt.Printf("Baixando JRE para %s/%s...\n", runtime.GOOS, runtime.GOARCH)

	destino := filepath.Join(hubsaudeDir(), "jre-download")
	if err := baixarArquivo(url, destino); err != nil {
		return fmt.Errorf("erro ao baixar JRE: %v", err)
	}

	fmt.Println("JRE baixado. Extração necessária — a implementar.")
	return nil
}

func jreURLParaPlataforma(info *ReleaseInfo) string {
	switch runtime.GOOS {
	case "windows":
		return info.JRE.WindowsX64
	case "linux":
		return info.JRE.LinuxX64
	case "darwin":
		return info.JRE.MacX64
	}
	return ""
}

func baixarArquivo(url string, destino string) error {
	if err := os.MkdirAll(filepath.Dir(destino), 0755); err != nil {
		return err
	}

	resp, err := http.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("status %d ao baixar %s", resp.StatusCode, url)
	}

	arquivo, err := os.Create(destino)
	if err != nil {
		return err
	}
	defer arquivo.Close()

	baixados, err := io.Copy(arquivo, resp.Body)
	if err != nil {
		return err
	}

	fmt.Printf("Download concluído (%.1f MB)\n", float64(baixados)/1024/1024)
	return nil
}