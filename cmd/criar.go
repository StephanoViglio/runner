package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var criarCmd = &cobra.Command{
	Use:   "criar",
	Short: "Cria uma assinatura digital via assinador.jar",
	Long: `Invoca o assinador.jar para simular a criação de uma assinatura digital.

Os parâmetros necessários seguem as especificações FHIR de segurança.

Exemplo:
  assinatura criar --conteudo <valor> --certificado <valor>`,
	Run: func(cmd *cobra.Command, args []string) {
		conteudo, _ := cmd.Flags().GetString("conteudo")
		certificado, _ := cmd.Flags().GetString("certificado")

		fmt.Printf("Criando assinatura...\n")
		fmt.Printf("  Conteudo:     %s\n", conteudo)
		fmt.Printf("  Certificado:  %s\n", certificado)
	},
}

func init() {
	rootCmd.AddCommand(criarCmd)

	criarCmd.Flags().String("conteudo", "", "Conteúdo a ser assinado (obrigatório)")
	criarCmd.Flags().String("certificado", "", "Certificado utilizado na assinatura (obrigatório)")

	criarCmd.MarkFlagRequired("conteudo")
	criarCmd.MarkFlagRequired("certificado")
}