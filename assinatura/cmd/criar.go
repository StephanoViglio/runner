package cmd

import (
	"github.com/spf13/cobra"
)

var criarCmd = &cobra.Command{
	Use:   "criar --arquivo <caminho>",
	Short: "Cria uma assinatura digital via assinador.jar",
	Long: `Cria uma assinatura digital no padrão JAdES/JWS a partir de um arquivo JSON.

O arquivo JSON deve conter os seguintes campos:

  Obrigatórios:
    bundle                 Recursos FHIR a serem assinados (entry com fullUrl e resourceJson)
    provenance             Referências dos recursos alvo da assinatura
    cryptographicMaterial  Estratégia e dados do dispositivo criptográfico (SMARTCARD ou TOKEN)
    certificateChain       Lista de certificados em Base64 (cadeia completa)
    referenceTimestamp     Timestamp Unix da operação (deve estar dentro de ±5 minutos do servidor)
    timestampStrategy      Estratégia de timestamp (ex: "iat")
    policyUri              URI da política no formato https://<uri>|<major.minor.patch>

Modos de execução:
  HTTP (padrão)  Envia o JSON para o servidor em execução via POST /sign
  CLI (--local)  Invoca o assinador.jar diretamente como subprocesso

Códigos de retorno:
  0  Assinatura criada com sucesso
  1  Erro de uso (parâmetro inválido, arquivo não encontrado, erro do backend)
  2  Erro inesperado

Exemplos:
  # Modo HTTP (padrão) — servidor deve estar em execução
  assinatura criar --arquivo requisicao.json

  # Modo CLI — invoca o jar diretamente
  assinatura criar --arquivo requisicao.json --local

  # Modo CLI com URL alternativa para download do jar
 assinatura criar --arquivo requisicao.json --local --source https://exemplo.com/assinador.jar`,
  Run: func(cmd *cobra.Command, args []string) {
		arquivo, _ := cmd.Flags().GetString("arquivo")
		local, _ := cmd.Flags().GetBool("local")
		source, _ := cmd.Flags().GetString("source")

		conteudo := lerArquivoJSON(arquivo)
		validarSintaxeCriar(conteudo)

		if local {
			invocarCLI("sign", arquivo, source)
		} else {
			invocarHTTP("/sign", conteudo)
		}
	},
}

func init() {
	rootCmd.AddCommand(criarCmd)

	criarCmd.Flags().String("arquivo", "", "Caminho para o arquivo JSON com os dados da requisição (obrigatório)")
	criarCmd.Flags().Bool("local", false, "Invoca o assinador.jar diretamente via CLI em vez de HTTP")
	criarCmd.Flags().String("source", defaultJarPath, "Caminho ou URL alternativa para o assinador.jar (usado apenas com --local)")

	criarCmd.MarkFlagRequired("arquivo")
}