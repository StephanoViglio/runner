package cmd

import (
	"os"

	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "simulador",
	Version: "0.1.0",
	Short: "CLI do Sistema Runner para gerenciamento do Simulador do HubSaúde",
	Long: `simulador é a interface de linha de comandos do Sistema Runner
para gerenciar o ciclo de vida do Simulador do HubSaúde.

Permite iniciar, parar e monitorar o simulador.jar sem necessidade
de conhecer os detalhes de configuração do ambiente Java.

Exemplos:
  simulador start
  simulador stop
  simulador status

Use "simulador [comando] --help" para mais informações sobre cada comando.`,
}

func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {}