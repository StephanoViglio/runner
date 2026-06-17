package cmd

import (
	"encoding/json"
	"fmt"
	"os"
	"regexp"
)

var regexPolicyUri = regexp.MustCompile(`^https://.+\|\d+\.\d+\.\d+$`)

type jsonCriar struct {
	Bundle                interface{} `json:"bundle"`
	Provenance            interface{} `json:"provenance"`
	CryptographicMaterial interface{} `json:"cryptographicMaterial"`
	CertificateChain      interface{} `json:"certificateChain"`
	ReferenceTimestamp    *float64    `json:"referenceTimestamp"`
	TimestampStrategy     *string     `json:"timestampStrategy"`
	PolicyUri             *string     `json:"policyUri"`
}

type jsonValidar struct {
	SignatureData      *string  `json:"signatureData"`
	ReferenceTimestamp *float64 `json:"referenceTimestamp"`
	PolicyUri          *string  `json:"policyUri"`
}

func coletarErrosCriar(conteudo []byte) []string {
	var req jsonCriar
	if err := json.Unmarshal(conteudo, &req); err != nil {
		return []string{fmt.Sprintf("JSON inválido: %v", err)}
	}

	var erros []string
	if req.Bundle == nil {
		erros = append(erros, "campo obrigatório ausente: bundle")
	}
	if req.Provenance == nil {
		erros = append(erros, "campo obrigatório ausente: provenance")
	}
	if req.CryptographicMaterial == nil {
		erros = append(erros, "campo obrigatório ausente: cryptographicMaterial")
	}
	if req.CertificateChain == nil {
		erros = append(erros, "campo obrigatório ausente: certificateChain")
	}
	if req.ReferenceTimestamp == nil {
		erros = append(erros, "campo obrigatório ausente: referenceTimestamp")
	}
	if req.TimestampStrategy == nil {
		erros = append(erros, "campo obrigatório ausente: timestampStrategy")
	}
	if req.PolicyUri == nil {
		erros = append(erros, "campo obrigatório ausente: policyUri")
	} else if !regexPolicyUri.MatchString(*req.PolicyUri) {
		erros = append(erros, fmt.Sprintf("policyUri com formato inválido: %q (esperado: https://<uri>|<major.minor.patch>)", *req.PolicyUri))
	}
	return erros
}

func coletarErrosValidar(conteudo []byte) []string {
	var req jsonValidar
	if err := json.Unmarshal(conteudo, &req); err != nil {
		return []string{fmt.Sprintf("JSON inválido: %v", err)}
	}

	var erros []string
	if req.SignatureData == nil {
		erros = append(erros, "campo obrigatório ausente: signatureData")
	}
	if req.ReferenceTimestamp == nil {
		erros = append(erros, "campo obrigatório ausente: referenceTimestamp")
	}
	if req.PolicyUri == nil {
		erros = append(erros, "campo obrigatório ausente: policyUri")
	} else if !regexPolicyUri.MatchString(*req.PolicyUri) {
		erros = append(erros, fmt.Sprintf("policyUri com formato inválido: %q (esperado: https://<uri>|<major.minor.patch>)", *req.PolicyUri))
	}
	return erros
}

func validarSintaxeCriar(conteudo []byte) {
	erros := coletarErrosCriar(conteudo)
	if len(erros) > 0 {
		for _, e := range erros {
			fmt.Fprintf(os.Stderr, "Erro de parâmetro: %s\n", e)
		}
		os.Exit(ExitErroUso)
	}
}

func validarSintaxeValidar(conteudo []byte) {
	erros := coletarErrosValidar(conteudo)
	if len(erros) > 0 {
		for _, e := range erros {
			fmt.Fprintf(os.Stderr, "Erro de parâmetro: %s\n", e)
		}
		os.Exit(ExitErroUso)
	}
}