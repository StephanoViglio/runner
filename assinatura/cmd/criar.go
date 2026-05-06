package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"

	"github.com/spf13/cobra"
)

var criarCmd = &cobra.Command{
	Use:   "criar",
	Short: "Cria uma assinatura digital via assinador.jar",
	Long: `Invoca o assinador.jar para simular a criação de uma assinatura digital.

O parâmetro --arquivo deve apontar para um JSON com os dados da requisição.

Exemplo:
  assinatura criar --arquivo requisicao.json`,
	Run: func(cmd *cobra.Command, args []string) {
		arquivo, _ := cmd.Flags().GetString("arquivo")

		// Lê o arquivo JSON
		conteudo, err := os.ReadFile(arquivo)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao ler o arquivo '%s': %v\n", arquivo, err)
			os.Exit(1)
		}

		// Valida se o conteúdo é um JSON válido
		if !json.Valid(conteudo) {
			fmt.Fprintf(os.Stderr, "Erro: o arquivo '%s' não contém um JSON válido\n", arquivo)
			os.Exit(1)
		}

		// Envia para o endpoint
		url := "http://localhost:8080/sign"
		resp, err := http.Post(url, "application/json", bytes.NewBuffer(conteudo))
		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao chamar o assinador: %v\n", err)
			os.Exit(1)
		}
		defer resp.Body.Close()

		// Lê o retorno
		corpo, err := io.ReadAll(resp.Body)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao ler resposta do assinador: %v\n", err)
			os.Exit(1)
		}

		// Trata erros HTTP
		if resp.StatusCode != http.StatusOK {
			fmt.Fprintf(os.Stderr, "Assinador retornou erro %d:\n%s\n", resp.StatusCode, string(corpo))
			os.Exit(1)
		}

		// Formata e exibe o retorno
		var resultado interface{}
		if err := json.Unmarshal(corpo, &resultado); err != nil {
			fmt.Println(string(corpo))
			return
		}

		saida, _ := json.MarshalIndent(resultado, "", "  ")
		fmt.Println(string(saida))
	},
}

func init() {
	rootCmd.AddCommand(criarCmd)

	criarCmd.Flags().String("arquivo", "", "Caminho para o arquivo JSON com os dados da requisição (obrigatório)")
	criarCmd.MarkFlagRequired("arquivo")
}