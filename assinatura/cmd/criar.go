package cmd

import (
	"github.com/spf13/cobra"
)

var criarCmd = &cobra.Command{
	Use:   "criar",
	Short: "Cria uma assinatura digital via assinador.jar",
	Long: `Invoca o assinador.jar para simular a criação de uma assinatura digital.

Por padrão utiliza o modo HTTP (servidor deve estar em execução).
Use --local para invocar o assinador.jar diretamente via linha de comando.

Exemplos:
  assinatura criar --arquivo requisicao.json
  assinatura criar --arquivo requisicao.json --local
  assinatura criar --arquivo requisicao.json --local --jar /caminho/assinador.jar`,
	Run: func(cmd *cobra.Command, args []string) {
		arquivo, _ := cmd.Flags().GetString("arquivo")
		local, _ := cmd.Flags().GetBool("local")
		jar, _ := cmd.Flags().GetString("jar")

		if local {
			invocarCLI("sign", arquivo, jar)
		} else {
			conteudo := lerArquivoJSON(arquivo)
			invocarHTTP("/sign", conteudo)
		}
	},
}

func init() {
	rootCmd.AddCommand(criarCmd)

	criarCmd.Flags().String("arquivo", "", "Caminho para o arquivo JSON com os dados da requisição (obrigatório)")
	criarCmd.Flags().Bool("local", false, "Invoca o assinador.jar diretamente via CLI em vez de HTTP")
	criarCmd.Flags().String("jar", defaultJarPath, "Caminho para o assinador.jar (usado apenas com --local)")

	criarCmd.MarkFlagRequired("arquivo")
}