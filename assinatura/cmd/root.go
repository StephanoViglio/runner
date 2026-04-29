package cmd

import (
	"os"

	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "assinatura",
	Short: "CLI do Sistema Runner para operações de assinatura digital",
	Long: `assinatura é a interface de linha de comandos do Sistema Runner.

Permite criar e validar assinaturas digitais invocando o assinador.jar,
sem necessidade de conhecer os detalhes de configuração do ambiente Java.

Exemplos:
  assinatura criar ...
  assinatura validar ...

Use "assinatura [comando] --help" para mais informações sobre cada comando.`,
}

func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {}
