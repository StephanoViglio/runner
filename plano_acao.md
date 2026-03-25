# Plano de Ação — Sistema Runner

## Visão Geral

Este plano organiza o desenvolvimento do **Sistema Runner** em fases iterativas, mapeando as entregas esperadas, dependências técnicas e ordem de execução das tarefas.
O sistema é composto por duas aplicações principais: `assinatura` (CLI multiplataforma) e `assinador.jar` (aplicação Java),
além do gerenciamento do `simulador.jar`.

---

## Iteração 0 — Preparação

O objetivo desta preparação não é escrever código de negócio, mas garantir a configuração antes do desenvolvimento.

### Repositório e convenções

- Definir estrutura do repositório (cada aplicação em um diretório)
- Configurar branches: `main` protegida, features em `feature/nome-da-feature`
- Definir convenção de commits (ex: Conventional Commits)
- Configurar o `.gitignore` para Go e Java

### Ambiente

- Go instalado e configurado
- Java + Maven instalados
- Decidir o framework CLI em Go (ideia inicial: `cobra`)
- Decidir o framework web Java para o modo servidor (Spring Boot)

### Divisão inicial de responsabilidades

| Pessoa | Foco principal |
|--------|---------------|
| Pessoa 1 | `assinador.jar` (Java) |
| Pessoa 2 | CLIs em Go |
| Ambos | Integração entre as partes |

---

## Iteração 1 — Esqueleto Funcional

O objetivo é ter o fluxo básico ponta a ponta funcionando, mesmo que de forma simples. Sem provisionamento automático de JDK, sem PKCS#11 — apenas o caminho principal.

### `assinador.jar`

- Criar projeto Maven com estrutura de pacotes
- Definir a interface `SignatureService` com os métodos `sign` e `validate`
- Implementar `FakeSignatureService` retornando respostas fixas pré-construídas
- Expor os dois modos de execução:
  - **Modo CLI**: aceita argumentos e imprime resultado no stdout
  - **Modo servidor**: Spring Boot com endpoints `POST /sign` e `POST /validate`
- Escrever testes unitários para os componentes implementados nesta iteração

### `assinador` CLI (Go)

- Criar projeto Go com `cobra` (analisar se utilizaremos essa biblioteca)
- Implementar comandos `criar` e `validar` com parâmetros básicos
- Criar comandos para invocar o `assinador.jar` via CLI ou HTTP (definir qual vai ser o default)
- Exibir o resultado formatado no terminal

### Resultado esperado

> O usuário consegue digitar um comando no CLI e receber uma resposta simulada do `.jar`.

---

## Iteração 2 — Validação dos Parâmetros de Entrada e Tratamento de Erros

Com o fluxo básico funcionando, o foco passa para a segunda parte do `assinador.jar`: validar os parâmetros corretamente conforme a especificação informada no README. Os testes unitários e de integração continuam sendo escritos junto com o código ao longo desta iteração.

### `assinador.jar`

- Estudar e mapear todos os parâmetros das especificações FHIR:
  - [Criar Assinatura](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-criar-assinatura.html)
  - [Validar Assinatura](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-validar-assinatura.html)
- Implementar validação completa dos parâmetros de entrada para `sign` e `validate`
- Retornar mensagens de erro claras e estruturadas quando algum parâmetro for inválido
- Aprofundar a cobertura de testes unitários, incluindo cenários de parâmetros válidos e inválidos
- Escrever testes de integração cobrindo os cenários principais
- Uso do SonarQube para avaliação da cobertura de testes e possíveis code smells

### `assinador` CLI (Go)

- Implementar validação sintática dos parâmetros antes de chamar o `.jar` (primeira barreira de erros)
- Melhorar o tratamento e exibição de erros vindos do `.jar`
- Implementar `--help` com descrição clara de cada parâmetro

### Resultado esperado

> O sistema rejeita entradas malformadas com mensagens úteis e aceita apenas requisições bem formadas.

---

## Iteração 3 — Simulador CLI e Provisionamento de JDK

Com o `assinador` estável, adiciona-se as outras duas peças de infraestrutura.

### `simulador` CLI (Go)

- Implementar comandos `start`, `stop` e `status`
- Gerenciar o processo Java (`simulador.jar`) como subprocesso
- Armazenar PID e porta em uso no diretório `~/.hubsaude`
- Detectar se o `simulador.jar` está presente; baixar se não estiver

### Provisionamento de JDK (compartilhado entre os dois CLIs)

- Implementar lógica de detecção do JDK em `~/.hubsaude/jdk`
- Implementar download do JDK para as três plataformas (Windows, Linux, macOS)
- Descompactar e configurar o JDK para uso local
- Encapsular essa lógica em um pacote Go reutilizável pelos dois CLIs

### Resultado esperado

> Ambos os CLIs funcionam do zero em uma máquina sem Java instalado.

---

## Iteração 4 — CI/CD, Binários e Documentação

A última iteração foca em qualidade, entrega e documentação.

### CI/CD com GitHub Actions

- Pipeline de CI: compilar + rodar testes a cada push
- Pipeline de CD: ao criar uma tag `v*`, gerar os binários para as três plataformas e publicar no GitHub Releases
- Incluir checksums SHA256 para cada binário

### Binários gerados

| Arquivo | Plataforma |
|---------|-----------|
| `assinatura-windows-amd64.exe` | Windows |
| `assinatura-linux-amd64` | Linux |
| `assinatura-darwin-amd64` | macOS |

> O cross-compiling é nativo em Go — basta configurar `GOOS` e `GOARCH`.

### Documentação

- `README.md` com guia de instalação e uso
- Exemplos de comandos para criar e validar assinatura
- Documentação técnica da integração entre CLI e `.jar`

### Testes de integração

- Cenários ponta a ponta cobrindo fluxo de criação e validação
- Cenários de erro: parâmetro inválido, JDK ausente, processo já em execução

### Resultado esperado

> Projeto documentado, binários publicados no GitHub Releases e pipeline de CI/CD funcionando.

---

## Resumo das Iterações

| Iteração | Foco principal | Entregável |
|----------|---------------|------------|
| 0 | Setup | Repositório e ambiente configurados |
| 1 | Esqueleto | Fluxo básico ponta a ponta funcionando |
| 2 | Validação dos Parâmetros | Parâmetros validados, erros tratados |
| 3 | Simulador + JDK | CLIs autossuficientes |
| 4 | CI/CD + docs | Binários publicados, projeto documentado |

---
