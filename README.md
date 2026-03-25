# Sistema Runner

> Projeto desenvolvido para a disciplina de **Implementação e Integração de Software** do curso de Engenharia de Software no período 2026.1

---

## Integrantes

| Nome | Matrícula |
|------|-----------|
| Omar Al Jawabri | 202403088 |
| Stephano Soares Viglio | 202403094 |

---

## Entregáveis

| Entregável | Critério de Conclusão |
|---|---|
| Repositório configurado + CI/CD base | Pipeline rodando sem erros |
| `assinador.jar` funcional | Todos os critérios de US-02 cobertos |
| CLI `assinatura` funcional | Todos os critérios de US-01 e US-04 cobertos |
| CLI `simulador` funcional | Todos os critérios de US-03 e US-04 cobertos |
| Suite de testes completa | Cobertura ≥ 70%, todos os critérios de aceitação passando |
| Binários publicados e assinados | Release no GitHub com `.sig`, `.pem` e `SHA256SUMS` |
| Documentação completa | Manual, docs técnicas e Readme publicados |

---

## Definições Técnicas

### Política de Branches

A branch `main` está protegida contra commits diretos via configuração no GitHub. O fluxo de trabalho segue a seguinte hierarquia:

```
main  ←  (Pull Request de branch de ação)  ←  feature/* | fix/* | refactor/* | chore/*
```

- **`main`** — branch principal, simula o ambiente de produção. Só recebe código via Pull Request de branches de ação.
- **Branches de ação** — criadas a partir de `main`, seguindo o padrão `tipo/descricao` (ex: `feature/assinar-arquivo`, `fix/validacao-cert`). Após conclusão, são mergeadas em `main` via Pull Request.

### Estrutura de Pastas (Assinador)

```
assinador/
├── cli/              # Entrada via linha de comando
├── http/             # Modo servidor (REST)
├── application/      # Casos de uso
├── domain/           # Regras e validações de negócio
└── infrastructure/   # Parsing, utilitários, etc.
```

### Stack Tecnológica

#### Assinador (`assinador.jar`)

| Tecnologia | Finalidade |
|---|---|
| Java | Linguagem principal |
| Spring Framework | Requisições HTTP (critério US-01) |
| picocli | Interface de linha de comando (critério US-01) |
| JUnit + Mockito | Testes unitários |
| Spring Boot Test | Testes de integração HTTP |
| SonarQube | Cobertura de testes e análise de code smells |

#### Interface de Linha de Comandos (CLI)

Possivelmente uso da linguagem Go para CLI de assinatura e simulador.

---

## Documentação

Diagrama C4.

---

## Pipeline CI/CD

Uso de GitHub Actions para entrega continua da aplicação.
