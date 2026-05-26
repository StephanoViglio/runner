package cmd

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
)

var statusCmd = &cobra.Command{
	Use:   "status",
	Short: "Exibe o status atual do Simulador do HubSaúde",
	Long: `Consulta o endpoint /api/info do simulador e exibe o status atual.

Exemplo:
  simulador status`,
	Run: func(cmd *cobra.Command, args []string) {
		state, err := lerState()
		if err != nil {
			fmt.Println("Status: parado (nenhuma instância registrada)")
			os.Exit(0)
		}

		url := fmt.Sprintf("http://localhost:%d/api/info", state.Port)
		resp, err := http.Get(url)
		if err != nil {
			fmt.Printf("Status: não responsivo (PID %d registrado, mas sem resposta na porta %d)\n",
				state.PID, state.Port)
			os.Exit(0)
		}
		defer resp.Body.Close()

		var info interface{}
		if err := json.NewDecoder(resp.Body).Decode(&info); err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao ler resposta do simulador: %v\n", err)
			os.Exit(1)
		}

		saida, _ := json.MarshalIndent(info, "", "  ")
		fmt.Printf("Status: em execução (PID %d, porta %d)\n\n", state.PID, state.Port)
		fmt.Println(string(saida))
	},
}

func init() {
	rootCmd.AddCommand(statusCmd)
}