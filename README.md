# Sistema Runner
> Projeto desenvolvido para a disciplina de **Implementação e Integração de Software** do curso de Engenharia de Software no período 2026.1

---

## Integrantes

| Nome | Matrícula |
|------|-----------|
| Omar Al Jawabri | 202403088 |
| Stephano Soares Viglio | 202403094 |

---

## Status do Projeto

O projeto está em desenvolvimento iterativo. A tabela abaixo reflete o estado atual de cada entregável.

### Iteração 0 — Preparação ✅ Concluída

| Tarefa | Status |
|--------|--------|
| Repositório criado e estrutura de pastas definida | ✅ Concluído |
| Branches configuradas e `main` protegida | ✅ Concluído |
| Convenção de commits definida (Conventional Commits) | ✅ Concluído |
| `.gitignore` configurado para Go e Java | ✅ Concluído |
| Frameworks definidos (`cobra` para Go, Spring Boot para Java) | ✅ Concluído |

### Iteração 1 — Esqueleto Funcional 🔄 Em andamento

| Tarefa | Responsável | Status |
|--------|-------------|--------|
| Projeto Go criado com `cobra` | Stephano | ✅ Concluído |
| Subcomandos `criar` e `validar` com parâmetros básicos | Stephano | ✅ Concluído |
| Validação de parâmetros obrigatórios no CLI | Stephano | ✅ Concluído |
| Invocação do `assinador.jar` via CLI ou HTTP | Stephano | ✅ Concluído |
| Projeto Maven do `assinador.jar` criado | Omar | ✅ Concluído |
| Interface `SignatureService` definida | Omar | ✅ Concluído |
| `FakeSignatureService` com respostas pré-construídas | Omar | ✅ Concluído |
| Modo CLI no `assinador.jar` | Omar | ✅ Concluído |
| Modo servidor HTTP (`/sign` e `/validate`) | Omar | ✅ Concluído |
| Testes unitários iniciais do `assinador.jar` | Omar | 🔄 Em andamento |

### Iteração 2 — Validação de Parâmetros e Tratamento de Erros 🔄 Em andamento

| Tarefa | Status |
|--------|--------|
| Mapeamento dos parâmetros das especificações FHIR | ✅ Concluído |
| Validação completa de parâmetros no `assinador.jar` | ✅ Concluído |
| Mensagens de erro estruturadas | ✅ Concluído |
| Validação sintática de parâmetros no CLI | ⏳ Não iniciado |
| `--help` detalhado com exemplos por subcomando | ✅ Concluído |
| Padronização de saída e exit codes | ✅ Concluído |
| Aprofundamento de testes unitários e de integração | ⏳ Não iniciado |
| Integração com SonarQube | 🔄 Em andamento |

### Iteração 3 — CI/CD e Binários ⏳ Não iniciada

| Tarefa | Status |
|--------|--------|
| Pipeline de CI com GitHub Actions | ⏳ Não iniciado |
| Pipeline de CD com geração de binários multiplataforma | ⏳ Não iniciado |
| Publicação no GitHub Releases com checksums SHA256 | ⏳ Não iniciado |
| Versionamento semântico (SemVer) | ⏳ Não iniciado |

### Iteração 4 — Simulador CLI e Provisionamento de JDK 🔄 Em andamento

| Tarefa | Status |
|--------|--------|
| CLI `simulador` com comandos `start`, `stop` e `status` | ✅ Concluído |
| Verificação de porta 8443 antes de iniciar | ✅ Concluído |
| Download do `simulador.jar` quando ausente | ✅ Concluído |
| Comparação de versão local vs remota via `release.json` | ✅ Concluído |
| Download do JRE via Eclipse Temurin | ✅ Concluído |
| Persistência de PID e porta em `~/.hubsaude` | ✅ Concluído |
| Detecção e download automático do JDK em `~/.hubsaude/jdk` | ⏳ Não iniciado |
| Provisionamento de JDK para Windows, Linux e macOS | ⏳ Não iniciado |
| Pacote Go compartilhado para provisionamento | ⏳ Não iniciado |

### Iteração 5 — Documentação e Qualidade ⏳ Não iniciada

| Tarefa | Status |
|--------|--------|
| README com instruções completas de uso | ⏳ Não iniciado |
| Documentação dos comandos do CLI | ⏳ Não iniciado |
| Documentação técnica da integração CLI ↔ `assinador.jar` | ⏳ Não iniciado |
| Testes ponta a ponta | ⏳ Não iniciado |
| Validação de cenários de erro | ⏳ Não iniciado |

---

## Entregáveis

| Entregável | Critério de Conclusão | Status |
|---|---|---|
| Repositório configurado + CI/CD base | Pipeline rodando sem erros | ⏳ Não iniciado |
| `assinador.jar` funcional | Todos os critérios de US-02 cobertos | 🔄 Em andamento |
| CLI `assinatura` funcional | Todos os critérios de US-01 cobertos | 🔄 Em andamento |
| CLI `simulador` funcional | Todos os critérios de US-03 cobertos | 🔄 Em andamento |
| Provisionamento de JDK | Todos os critérios de US-04 cobertos | ⏳ Não iniciado |
| Suite de testes completa | Cobertura ≥ 70%, todos os critérios de aceitação passando | ⏳ Não iniciado |
| Binários publicados | Release no GitHub com checksums SHA256 | ⏳ Não iniciado |
| Documentação completa | Manual, docs técnicas e README publicados | ⏳ Não iniciado |

---

## Definições Técnicas

### Política de Branches

A branch `main` está protegida contra commits diretos via configuração no GitHub. O fluxo de trabalho segue a seguinte hierarquia:

```
main  ←  (Pull Request de branch de ação)  ←  feature/* | fix/* | refactor/* | chore/*
```

- **`main`** — branch principal, simula o ambiente de produção. Só recebe código via Pull Request.
- **Branches de ação** — criadas a partir de `main`, seguindo o padrão `tipo/descricao` (ex: `feature/assinar-arquivo`, `fix/validacao-cert`).

### Arquitetura

A aplicação Java (`assinador.jar`) segue Arquitetura Hexagonal com Onion Architecture, organizada em quatro camadas (`domain`, `application`, `presentation`, `infrastructure`).

Para detalhes sobre a estrutura de pastas, responsabilidades de cada camada, portas (ports) e regra de dependência, consulte [arquitetura.md](docs/arquitetura.md).

A CLI Go (`assinatura`) segue uma estrutura simples baseada em `cobra`:

​```
assinatura/
├── cmd/
│   ├── root.go       # Comando raiz
│   ├── criar.go      # Subcomando criar
│   └── validar.go    # Subcomando validar
└── main.go
​```

### Stack Tecnológica

#### `assinador.jar` (Java)

| Tecnologia | Finalidade |
|---|---|
| Java | Linguagem principal |
| Spring Boot | Modo servidor HTTP (`/sign` e `/validate`) |
| picocli | Modo CLI |
| JUnit + Mockito | Testes unitários |
| Spring Boot Test | Testes de integração HTTP |
| SonarQube | Cobertura de testes e análise de code smells |

#### CLI `assinatura` (Go)

| Tecnologia | Finalidade |
|---|---|
| Go | Linguagem principal |
| cobra | Framework de CLI |

---

## Pipeline CI/CD

A ser configurado na Iteração 3 via GitHub Actions, cobrindo:
- Compilação e execução de testes a cada push
- Geração de binários multiplataforma ao criar tag `v*`
- Publicação automática no GitHub Releases

---

## Documentação

A ser desenvolvida na Iteração 5. Incluirá diagramas C4 (Contexto e Contêineres), manual de uso e documentação técnica da integração.
