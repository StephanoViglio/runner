package cmd

import (
	"github.com/spf13/cobra"
)

var validarCmd = &cobra.Command{
	Use:   "validar --arquivo <caminho>",
	Short: "Valida uma assinatura digital via assinador.jar",
	Long: `Valida uma assinatura digital no padrão JAdES/JWS a partir de um arquivo JSON.

O arquivo JSON deve conter os seguintes campos:

  Obrigatórios:
    signatureData       Assinatura digital em Base64 (campo "data" retornado pelo comando criar)
    referenceTimestamp  Timestamp Unix da operação (deve estar dentro de ±5 minutos do servidor)
    policyUri           URI da política no formato https://<uri>|<major.minor.patch>

  Opcionais:
    bundle      Recursos FHIR originais — quando fornecido, verifica também a integridade do conteúdo
    provenance  Referências dos recursos alvo — deve ser fornecido junto com bundle

Modos de execução:
  HTTP (padrão)  Envia o JSON para o servidor em execução via POST /validate
  CLI (--local)  Invoca o assinador.jar diretamente como subprocesso

Códigos de retorno:
  0  Validação concluída com sucesso
  1  Erro de uso (parâmetro inválido, arquivo não encontrado, erro do backend)
  2  Erro inesperado

Exemplos:
  # Modo HTTP (padrão) — servidor deve estar em execução
  assinatura validar --arquivo validacao.json

  # Modo CLI — invoca o jar diretamente
  assinatura validar --arquivo validacao.json --local

  # Modo CLI com URL alternativa para download do jar
  assinatura validar --arquivo validacao.json --local --source https://exemplo.com/assinador.jar`,
	Run: func(cmd *cobra.Command, args []string) {
		arquivo, _ := cmd.Flags().GetString("arquivo")
		local, _ := cmd.Flags().GetBool("local")
		source, _ := cmd.Flags().GetString("source")

		if local {
			invocarCLI("validate", arquivo, source)
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
	validarCmd.Flags().String("source", defaultJarPath, "Caminho ou URL alternativa para o assinador.jar (usado apenas com --local)")

	validarCmd.MarkFlagRequired("arquivo")
}