package cmd

import (
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
)

var stopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Para o Simulador do HubSaúde",
	Long: `Envia requisição de encerramento ao simulador via endpoint /shutdown.

Exemplo:
  simulador stop`,
	Run: func(cmd *cobra.Command, args []string) {
		state, err := lerState()
		if err != nil {
			fmt.Println("Simulador não está em execução.")
			os.Exit(0)
		}

		url := fmt.Sprintf("http://localhost:%d/shutdown", state.Port)
		fmt.Printf("Enviando sinal de parada para %s...\n", url)

		resp, err := http.Post(url, "application/json", nil)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao parar simulador: %v\n", err)
			os.Exit(1)
		}
		defer resp.Body.Close()

		if err := limparState(); err != nil {
			fmt.Fprintf(os.Stderr, "Aviso: não foi possível limpar estado: %v\n", err)
		}

		fmt.Println("Simulador encerrado com sucesso.")
	},
}

func init() {
	rootCmd.AddCommand(stopCmd)
}