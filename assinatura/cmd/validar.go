package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var validarCmd = &cobra.Command{
	Use:   "validar",
	Short: "Valida uma assinatura digital via assinador.jar",
	Long: `Invoca o assinador.jar para simular a validação de uma assinatura digital.

Os parâmetros necessários seguem as especificações FHIR de segurança.

Exemplo:
  assinatura validar --conteudo <valor> --assinatura <valor>`,
	Run: func(cmd *cobra.Command, args []string) {
		conteudo, _ := cmd.Flags().GetString("conteudo")
		assinatura, _ := cmd.Flags().GetString("assinatura")

		fmt.Printf("Validando assinatura...\n")
		fmt.Printf("  Conteudo:    %s\n", conteudo)
		fmt.Printf("  Assinatura:  %s\n", assinatura)
	},
}

func init() {
	rootCmd.AddCommand(validarCmd)

	validarCmd.Flags().String("conteudo", "", "Conteúdo original que foi assinado (obrigatório)")
	validarCmd.Flags().String("assinatura", "", "Assinatura digital a ser validada (obrigatório)")

	validarCmd.MarkFlagRequired("conteudo")
	validarCmd.MarkFlagRequired("assinatura")
}