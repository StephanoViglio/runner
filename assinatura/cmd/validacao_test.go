package cmd

import (
	"testing"
	"strings"
)

func TestColetarErrosCriarValido(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT",
		"policyUri": "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.2"
	}`)
	erros := coletarErrosCriar(json)
	if len(erros) != 0 {
		t.Errorf("esperava nenhum erro, recebeu: %v", erros)
	}
}

func TestColetarErrosCriarBundleAusente(t *testing.T) {
	json := []byte(`{
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT",
		"policyUri": "https://exemplo.com|1.0.0"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "bundle") {
		t.Errorf("esperava erro sobre 'bundle', recebeu: %v", erros)
	}
}

func TestColetarErrosCriarPolicyUriAusente(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "policyUri") {
		t.Errorf("esperava erro sobre 'policyUri', recebeu: %v", erros)
	}
}

func TestColetarErrosCriarPolicyUriSemHttps(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT",
		"policyUri": "http://exemplo.com|1.0.0"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "policyUri com formato inválido") {
		t.Errorf("esperava erro de formato de policyUri, recebeu: %v", erros)
	}
}

func TestColetarErrosCriarPolicyUriSemVersao(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT",
		"policyUri": "https://exemplo.com"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "policyUri com formato inválido") {
		t.Errorf("esperava erro de formato de policyUri, recebeu: %v", erros)
	}
}

func TestColetarErrosCriarPolicyUriVersaoNaoNumerica(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"referenceTimestamp": 1778634475,
		"timestampStrategy": "IAT",
		"policyUri": "https://exemplo.com|abc"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "policyUri com formato inválido") {
		t.Errorf("esperava erro de formato de policyUri, recebeu: %v", erros)
	}
}

func TestColetarErrosCriarMultiplosCamposAusentes(t *testing.T) {
	json := []byte(`{}`)
	erros := coletarErrosCriar(json)
	if len(erros) < 2 {
		t.Errorf("esperava múltiplos erros, recebeu: %v", erros)
	}
}

func TestColetarErrosCriarTimestampAusente(t *testing.T) {
	json := []byte(`{
		"bundle": {},
		"provenance": {},
		"cryptographicMaterial": {},
		"certificateChain": [],
		"timestampStrategy": "IAT",
		"policyUri": "https://exemplo.com|1.0.0"
	}`)
	erros := coletarErrosCriar(json)
	if !contemErro(erros, "referenceTimestamp") {
		t.Errorf("esperava erro sobre 'referenceTimestamp', recebeu: %v", erros)
	}
}

func TestColetarErrosValidarValido(t *testing.T) {
	json := []byte(`{
		"signatureData": "base64==",
		"referenceTimestamp": 1778634475,
		"policyUri": "https://exemplo.com|1.0.0"
	}`)
	erros := coletarErrosValidar(json)
	if len(erros) != 0 {
		t.Errorf("esperava nenhum erro, recebeu: %v", erros)
	}
}

func TestColetarErrosValidarSignatureDataAusente(t *testing.T) {
	json := []byte(`{
		"referenceTimestamp": 1778634475,
		"policyUri": "https://exemplo.com|1.0.0"
	}`)
	erros := coletarErrosValidar(json)
	if !contemErro(erros, "signatureData") {
		t.Errorf("esperava erro sobre 'signatureData', recebeu: %v", erros)
	}
}

func TestColetarErrosValidarPolicyUriInvalida(t *testing.T) {
	json := []byte(`{
		"signatureData": "base64==",
		"referenceTimestamp": 1778634475,
		"policyUri": "ftp://errado|1.0.0"
	}`)
	erros := coletarErrosValidar(json)
	if !contemErro(erros, "policyUri com formato inválido") {
		t.Errorf("esperava erro de formato de policyUri, recebeu: %v", erros)
	}
}

func contemErro(erros []string, substring string) bool {
    for _, e := range erros {
        if strings.Contains(e, substring) {
            return true
        }
    }
    return false
}