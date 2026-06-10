package jdk

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
)

const (
	jreWindowsX64URL = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse"
	jreLinuxX64URL   = "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jre/hotspot/normal/eclipse"
	jreMacX64URL     = "https://api.adoptium.net/v3/binary/latest/21/ga/mac/x64/jre/hotspot/normal/eclipse"
)

func GarantirJDK(hubsaudeDir string) error {
	if javaPath, err := EncontrarJava(hubsaudeDir); err == nil {
		fmt.Printf("JRE já disponível: %s\n", javaPath)
		return nil
	}

	url := jreURLParaPlataforma()
	if url == "" {
		return fmt.Errorf("plataforma não suportada: %s/%s", runtime.GOOS, runtime.GOARCH)
	}

	fmt.Printf("Baixando JRE para %s/%s...\n", runtime.GOOS, runtime.GOARCH)

	ext := ".tar.gz"
	if runtime.GOOS == "windows" {
		ext = ".zip"
	}

	arquivoDownload := filepath.Join(hubsaudeDir, "jre-download"+ext)
	if err := BaixarArquivo(url, arquivoDownload); err != nil {
		return fmt.Errorf("erro ao baixar JRE: %v", err)
	}

	fmt.Println("Extraindo JRE...")

	jreDir := filepath.Join(hubsaudeDir, "jre")
	if err := os.MkdirAll(jreDir, 0755); err != nil {
		return fmt.Errorf("erro ao criar diretório JRE: %v", err)
	}

	var errExtracao error
	if runtime.GOOS == "windows" {
		errExtracao = extrairZIP(arquivoDownload, jreDir)
	} else {
		errExtracao = extrairTarGZ(arquivoDownload, jreDir)
	}

	os.Remove(arquivoDownload)

	if errExtracao != nil {
		return fmt.Errorf("erro ao extrair JRE: %v", errExtracao)
	}

	javaPath, err := EncontrarJava(hubsaudeDir)
	if err != nil {
		return fmt.Errorf("JRE extraído mas java não encontrado: %v", err)
	}

	fmt.Printf("JRE configurado: %s\n", javaPath)
	return nil
}

func CaminhoJava(hubsaudeDir string) string {
	if path, err := EncontrarJava(hubsaudeDir); err == nil {
		return path
	}
	fmt.Fprintln(os.Stderr, "Aviso: JRE local não encontrado, usando java do sistema")
	return "java"
}

func EncontrarJava(hubsaudeDir string) (string, error) {
	javaBin := "java"
	if runtime.GOOS == "windows" {
		javaBin = "java.exe"
	}

	jreDir := filepath.Join(hubsaudeDir, "jre")
	var javaPath string
	err := filepath.Walk(jreDir, func(path string, info os.FileInfo, err error) error {
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
		return "", fmt.Errorf("java não encontrado em %s", jreDir)
	}

	return javaPath, nil
}

func BaixarArquivo(url string, destino string) error {
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

func jreURLParaPlataforma() string {
	switch runtime.GOOS {
	case "windows":
		return jreWindowsX64URL
	case "linux":
		return jreLinuxX64URL
	case "darwin":
		return jreMacX64URL
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