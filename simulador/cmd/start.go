package cmd

import (
	"fmt"
	"net"
	"net/http"
	"os"
	"os/exec"
	"time"

	"github.com/spf13/cobra"
)

const portaPadrao = 8443
const startupTimeout = 10 * time.Second

var startCmd = &cobra.Command{
	Use:   "start",
	Short: "Inicia o Simulador do HubSaúde",
	Long: `Inicia o simulador.jar como subprocesso.

Antes de iniciar, verifica se:
  - A porta 8443 está disponível
  - O simulador.jar está presente (baixa se necessário)
  - O JRE está disponível (baixa se necessário)

Exemplo:
  simulador start`,
	Run: func(cmd *cobra.Command, args []string) {
		// Verifica se já está em execução
		state, err := lerState()
		if err == nil && state.PID > 0 {
    	if instanciaViva(state.Port) {
        fmt.Printf("Simulador já está em execução e respondendo (PID %d, porta %d)\n",
            state.PID, state.Port)
        os.Exit(0)
    }
    fmt.Println("Aviso: estado anterior encontrado mas simulador não responde. Reiniciando...")
    _ = limparState()
}

		// Verifica disponibilidade da porta
		fmt.Printf("Verificando porta %d...\n", portaPadrao)
		if !portaDisponivel(portaPadrao) {
			fmt.Fprintf(os.Stderr, "Erro: porta %d já está em uso por outro processo\n", portaPadrao)
			fmt.Fprintf(os.Stderr, "Dica: encerre o processo que ocupa a porta ou use outro terminal para identificá-lo\n")
			os.Exit(1)
		}
		fmt.Printf("Porta %d disponível.\n", portaPadrao)

		// Provisiona jar e JRE
		fmt.Println("Verificando dependências...")
		info, err := buscarReleaseInfo()
		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao buscar informações de release: %v\n", err)
			os.Exit(1)
		}

		if err := garantirJar(info); err != nil {
			fmt.Fprintf(os.Stderr, "%v\n", err)
			os.Exit(1)
		}

		if err := garantirJRE(info); err != nil {
			fmt.Fprintf(os.Stderr, "%v\n", err)
			os.Exit(1)
		}

		// Inicia o processo
		fmt.Println("Iniciando simulador...")
		processo := exec.Command("java", "-jar", jarLocalPath())
		processo.Stdout = os.Stdout
		processo.Stderr = os.Stderr

		if err := processo.Start(); err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao iniciar simulador: %v\n", err)
			os.Exit(1)
		}

		// Salva estado
		novoState := &SimuladorState{
			PID:     processo.Process.Pid,
			Port:    portaPadrao,
			Version: info.Jar.Version,
		}
		if err := salvarState(novoState); err != nil {
			fmt.Fprintf(os.Stderr, "Aviso: não foi possível salvar estado: %v\n", err)
		}

		fmt.Printf("Simulador iniciado (PID %d, porta %d)\n", processo.Process.Pid, portaPadrao)
	},
}

func instanciaViva(porta int) bool {
    client := &http.Client{Timeout: startupTimeout}
    url := fmt.Sprintf("http://localhost:%d/api/info", porta)
    resp, err := client.Get(url)
    if err != nil {
        return false
    }
    defer resp.Body.Close()
    return resp.StatusCode == http.StatusOK
}

func portaDisponivel(porta int) bool {
	ln, err := net.Listen("tcp", fmt.Sprintf(":%d", porta))
	if err != nil {
		return false
	}
	ln.Close()
	return true
}

func init() {
	rootCmd.AddCommand(startCmd)
}