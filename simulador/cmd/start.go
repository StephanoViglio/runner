package cmd

import (
	"fmt"
	"net"
	"os"
	"os/exec"

	"github.com/spf13/cobra"
)

const portaPadrao = 8443

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
			fmt.Printf("Simulador já está em execução (PID %d, porta %d)\n", state.PID, state.Port)
			os.Exit(0)
		}

		// Verifica disponibilidade da porta
		fmt.Printf("Verificando porta %d...\n", portaPadrao)
		if !portaDisponivel(portaPadrao) {
			fmt.Fprintf(os.Stderr, "Erro: porta %d já está em uso\n", portaPadrao)
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