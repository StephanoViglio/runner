package cmd

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
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
	// Verifica se o java já está disponível e localizável
	if javaPath, err := encontrarJava(); err == nil {
		fmt.Printf("JRE já disponível: %s\n", javaPath)
		return nil
	}

	url := jreURLParaPlataforma(info)
	if url == "" {
		return fmt.Errorf("plataforma não suportada: %s/%s", runtime.GOOS, runtime.GOARCH)
	}

	fmt.Printf("Baixando JRE para %s/%s...\n", runtime.GOOS, runtime.GOARCH)

	ext := ".tar.gz"
	if runtime.GOOS == "windows" {
		ext = ".zip"
	}

	arquivoDownload := filepath.Join(hubsaudeDir(), "jre-download"+ext)
	if err := baixarArquivo(url, arquivoDownload); err != nil {
		return fmt.Errorf("erro ao baixar JRE: %v", err)
	}

	fmt.Println("Extraindo JRE...")

	if err := os.MkdirAll(jreDirPath(), 0755); err != nil {
		return fmt.Errorf("erro ao criar diretório JRE: %v", err)
	}

	var errExtracao error
	if runtime.GOOS == "windows" {
		errExtracao = extrairZIP(arquivoDownload, jreDirPath())
	} else {
		errExtracao = extrairTarGZ(arquivoDownload, jreDirPath())
	}

	os.Remove(arquivoDownload)

	if errExtracao != nil {
		return fmt.Errorf("erro ao extrair JRE: %v", errExtracao)
	}

	javaPath, err := encontrarJava()
	if err != nil {
		return fmt.Errorf("JRE extraído mas java não encontrado: %v", err)
	}

	fmt.Printf("JRE configurado: %s\n", javaPath)
	return nil
}

func encontrarJava() (string, error) {
	javaBin := "java"
	if runtime.GOOS == "windows" {
		javaBin = "java.exe"
	}

	var javaPath string
	err := filepath.Walk(jreDirPath(), func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() && info.Name() == javaBin {
			if filepath.Base(filepath.Dir(path)) == "bin" {
				javaPath = path
				return filepath.SkipAll
			}
		}
		return nil
	})

	if err != nil || javaPath == "" {
		return "", fmt.Errorf("java não encontrado em %s", jreDirPath())
	}

	return javaPath, nil
}

// caminhoJava retorna o caminho do java extraído, com fallback para o java do sistema
func caminhoJava() string {
	if path, err := encontrarJava(); err == nil {
		return path
	}
	fmt.Fprintln(os.Stderr, "Aviso: JRE local não encontrado, usando java do sistema")
	return "java"
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

func extrairZIP(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}
	defer r.Close()

	for _, f := range r.File {
		caminho := filepath.Join(dest, filepath.FromSlash(f.Name))

		if !strings.HasPrefix(caminho, filepath.Clean(dest)+string(os.PathSeparator)) {
			return fmt.Errorf("caminho inválido no zip: %s", f.Name)
		}

		if f.FileInfo().IsDir() {
			os.MkdirAll(caminho, f.Mode())
			continue
		}

		if err := os.MkdirAll(filepath.Dir(caminho), 0755); err != nil {
			return err
		}

		destArq, err := os.OpenFile(caminho, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return err
		}

		rc, err := f.Open()
		if err != nil {
			destArq.Close()
			return err
		}

		_, err = io.Copy(destArq, rc)
		rc.Close()
		destArq.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func extrairTarGZ(src, dest string) error {
	f, err := os.Open(src)
	if err != nil {
		return err
	}
	defer f.Close()

	gz, err := gzip.NewReader(f)
	if err != nil {
		return err
	}
	defer gz.Close()

	tr := tar.NewReader(gz)
	for {
		header, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		caminho := filepath.Join(dest, filepath.FromSlash(header.Name))

		if !strings.HasPrefix(caminho, filepath.Clean(dest)+string(os.PathSeparator)) {
			return fmt.Errorf("caminho inválido no tar: %s", header.Name)
		}

		switch header.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(caminho, 0755); err != nil {
				return err
			}
		case tar.TypeReg:
			if err := os.MkdirAll(filepath.Dir(caminho), 0755); err != nil {
				return err
			}
			destArq, err := os.OpenFile(caminho, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, os.FileMode(header.Mode))
			if err != nil {
				return err
			}
			_, err = io.Copy(destArq, tr)
			destArq.Close()
			if err != nil {
				return err
			}
		}
	}
	return nil
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