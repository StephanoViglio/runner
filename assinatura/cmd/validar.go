package cmd

import (
	"github.com/spf13/cobra"
)

var validarCmd = &cobra.Command{
	Use:   "validar",
	Short: "Valida uma assinatura digital via assinador.jar",
	Long: `Invoca o assinador.jar para validar uma assinatura digital no padrão JAdES/JWS.

Por padrão utiliza o modo HTTP (servidor deve estar em execução).
Use --local para invocar o assinador.jar diretamente via linha de comando.

Exemplos:
  assinatura validar --arquivo validacao.json
  assinatura validar --arquivo validacao.json --local
  assinatura validar --arquivo validacao.json --local --jar /caminho/assinador.jar`,
	Run: func(cmd *cobra.Command, args []string) {
		arquivo, _ := cmd.Flags().GetString("arquivo")
		local, _ := cmd.Flags().GetBool("local")
		jar, _ := cmd.Flags().GetString("jar")

		if local {
			invocarCLI("validate", arquivo, jar)
		} else {
			conteudo := lerArquivoJSON(arquivo)
			invocarHTTP("/validate", conteudo)
		}
	},
}

func init() {
	rootCmd.AddCommand(validarCmd)

	validarCmd.Flags().String("arquivo", "", "Caminho para o arquivo JSON com os dados da requisição (obrigatório)")
	validarCmd.Flags().Bool("local", false, "Invoca o assinador.jar diretamente via CLI em vez de HTTP")
	validarCmd.Flags().String("jar", defaultJarPath, "Caminho para o assinador.jar (usado apenas com --local)")

	validarCmd.MarkFlagRequired("arquivo")
}