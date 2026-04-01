# Backlog — Sistema Runner

## Convenções

- **Formato das tarefas**: BDD (Dado / Quando / Então)
- **"Assinador"** = `assinador.jar` (Java)
- **"CLI assinatura"** = aplicativo de linha de comando que invoca `assinador.jar`
- **"CLI simulador"** = aplicativo de linha de comando para obter/iniciar/parar/checar `simulador.jar` (via releases)

---

## Iteração 0 — Preparação

### Tarefas

1. Fazer setup inicial do Assinador
- Dado que o setup inicial foi realizado
    Quando for iniciado o desenvolvimento do Assinador
    Então já haverá uma base no repositório

2. Configurar branches e proteções
- Dado que as branches foram configuradas
    E as proteções no repositório foram definidas
    Quando for iniciado o desenvolvimento do sistema
    Então as branches já estão configuradas e seguras
 

## US-01 — Invocar assinador.jar via CLI

**Como** usuário do Sistema Runner
**Quero** executar comandos de assinatura digital através da linha de comandos
**Para que** eu possa invocar o `assinador.jar` sem conhecer detalhes de configuração Java, tanto para assinar quanto para validar

### Tarefas

1.  Configurar projeto Go e analisar biblioteca de CLI
   - Dado que o projeto Go foi criado
     Quando avaliamos as opções de biblioteca (`cobra`)
     Então a escolha está documentada e o projeto está estruturado para receber os subcomandos

2.  Implementar subcomandos `criar` e `validar` com parâmetros básicos
   - Dado que sou um usuário no terminal
     Quando executo `assinatura criar ...` ou `assinatura validar ...` com parâmetros básicos
     Então o CLI reconhece o subcomando e os parâmetros fornecidos
   - Dado que informo um comando inexistente
     Quando executo o CLI
     Então recebo erro claro e exit code diferente de zero

3. Implementar invocação do `assinador.jar` via CLI e HTTP e definir o modo padrão
   - Dado que o `assinador.jar` está disponível localmente
     Quando executo `assinatura criar ...` ou `assinatura validar ...`
     Então o CLI invoca o `assinador.jar` no modo padrão (a definir) e imprime o resultado formatado
   - Dado que o `assinador.jar` retorna erro
     Quando executo o comando
     Então o CLI apresenta mensagem de erro compreensível e encerra com exit code != 0

4. Implementar validação sintática dos parâmetros no CLI
   - Dado que omito um parâmetro obrigatório
     Quando executo o CLI
     Então recebo mensagem indicando o parâmetro ausente antes de invocar o `assinador.jar`
   - Dado que forneço um parâmetro com formato inválido
     Quando executo o CLI
     Então recebo mensagem de erro clara sem que o `assinador.jar` seja invocado

5. Implementar `--help` e padronizar saída
   - Dado que sou um usuário no terminal
     Quando executo `assinatura --help`, `assinatura criar --help` ou `assinatura validar --help`
     Então vejo descrição do comando, parâmetros obrigatórios/opcionais e exemplos de uso
   - Dado que a operação foi bem-sucedida
     Quando o CLI imprime o resultado
     Então a saída é legível e o exit code é zero
   - Dado que houve falha
     Quando o CLI finaliza
     Então retorna exit code != 0 e mensagem útil

---

## US-02 — Simular Assinatura Digital com Validação de Parâmetros

**Como** usuário do Sistema Runner
**Quero** que o Assinador valide rigorosamente os parâmetros de entrada antes de simular uma operação de assinatura digital
**Para que** eu receba feedback imediato sobre erros de parâmetros, garantindo que apenas requisições bem formadas sejam processadas

### Tarefas

1. Implementar validação de parâmetros para criação de assinatura
   - Dado que recebo parâmetros válidos para criação de assinatura
     Quando o Assinador os processa
     Então retorna a assinatura simulada pré-construída com sucesso
   - Dado que recebo parâmetros inválidos ou incompletos para criação de assinatura
     Quando o Assinador os processa
     Então retorna mensagem de erro estruturada indicando qual parâmetro está incorreto e o motivo

2. Implementar validação de parâmetros para validação de assinatura
   - Dado que recebo parâmetros válidos para validação de assinatura
     Quando o Assinador os processa
     Então retorna resultado simulado pré-determinado (válido ou inválido)
   - Dado que recebo parâmetros inválidos ou incompletos para validação de assinatura
     Quando o Assinador os processa
     Então retorna mensagem de erro estruturada indicando qual parâmetro está incorreto e o motivo

3. Implementar `FakeSignatureService` com respostas pré-construídas
   - Dado que todos os parâmetros de criação são válidos
     Quando o método `sign` é invocado
     Então retorna uma assinatura digital simulada fixa e bem formada
   - Dado que todos os parâmetros de validação são válidos
     Quando o método `validate` é invocado
     Então retorna resultado simulado fixo (sucesso ou falha) no formato esperado

4. Implementar suporte à interface PKCS#11 (simulado)
   - Dado que o Assinador recebe parâmetros que referenciam um dispositivo criptográfico
     Quando processa a requisição
     Então não falha por ausência do dispositivo físico e responde com resultado simulado

5. Garantir retorno estruturado em todos os cenários
   - Dado qualquer entrada (válida ou inválida)
     Quando o Assinador responde
     Então a saída segue formato estruturado consistente tanto no modo CLI quanto no modo servidor

---

## US-03 — Gerenciar Ciclo de Vida do Simulador do HubSaúde

**Como** usuário do Sistema Runner
**Quero** iniciar, parar e monitorar o Simulador do HubSaúde (`simulador.jar`) através do CLI
**Para que** eu possa gerenciar o ciclo de vida do Simulador sem conhecer os comandos Java subjacentes

### Tarefas

1. Implementar comando `start` do CLI simulador
   - Dado que o `simulador.jar` está disponível em `~/.hubsaude`
     Quando executo `simulador start`
     Então o processo Java é iniciado, o PID e a porta são armazenados em `~/.hubsaude` e o CLI confirma a inicialização
   - Dado que o simulador já está em execução
     Quando executo `simulador start`
     Então o CLI informa que o processo já está ativo e não inicia uma segunda instância

2. Implementar comando `stop` do CLI simulador
   - Dado que o simulador está em execução
     Quando executo `simulador stop`
     Então o processo é encerrado e o CLI confirma a parada
   - Dado que o simulador não está em execução
     Quando executo `simulador stop`
     Então o CLI informa que não há processo ativo para encerrar

3. Implementar comando `status` do CLI simulador
   - Dado que o simulador está em execução
     Quando executo `simulador status`
     Então o CLI exibe PID, porta em uso e tempo de execução
   - Dado que o simulador não está em execução
     Quando executo `simulador status`
     Então o CLI exibe que o processo está parado

4. Implementar download do `simulador.jar` quando ausente
   - Dado que o `simulador.jar` não está presente em `~/.hubsaude`
     Quando executo qualquer comando do CLI simulador
     Então o CLI baixa o `simulador.jar` da URL configurada antes de prosseguir
   - Dado que o usuário fornece `--source <url>`
     Quando executo o comando
     Então o CLI usa a URL fornecida em vez da URL padrão interna

---

## US-04 — Provisionar JDK Automaticamente

**Como** usuário do Sistema Runner
**Quero** que o sistema baixe e configure automaticamente o JDK necessário quando este não estiver disponível
**Para que** eu possa utilizar o Assinador e o Simulador sem precisar instalar ou configurar o Java manualmente

### Tarefas

1. Implementar detecção de JDK em `~/.hubsaude/jdk`
   - Dado que o JDK já está presente em `~/.hubsaude/jdk`
     Quando qualquer CLI é inicializado
     Então o JDK local é utilizado sem nenhuma ação adicional
   - Dado que o JDK não está presente em `~/.hubsaude/jdk`
     Quando qualquer CLI é inicializado
     Então o sistema inicia o processo de download automaticamente

2. Implementar download e descompactação do JDK por plataforma
   - Dado que estou em Windows (amd64)
     Quando o download do JDK é acionado
     Então o JDK compatível com Windows é baixado e descompactado em `~/.hubsaude/jdk`
   - Dado que estou em Linux (amd64)
     Quando o download do JDK é acionado
     Então o JDK compatível com Linux é baixado e descompactado em `~/.hubsaude/jdk`
   - Dado que estou em macOS (amd64)
     Quando o download do JDK é acionado
     Então o JDK compatível com macOS é baixado e descompactado em `~/.hubsaude/jdk`

3. Encapsular lógica de provisionamento em pacote Go reutilizável
   - Dado que ambos os CLIs precisam do JDK
     Quando a lógica de provisionamento é implementada
     Então ela reside em um pacote Go compartilhado, sem duplicação de código entre os dois CLIs

4. Exibir progresso do download ao usuário
   - Dado que o download do JDK está em andamento
     Quando o usuário aguarda
     Então o CLI exibe progresso legível (ex.: porcentagem ou indicador de atividade) e confirma ao concluir

---

## US-05 — Disponibilizar Binários Multiplataforma

**Como** usuário do Sistema Runner
**Quero** baixar uma versão pré-compilada do CLI para minha plataforma (Windows, Linux ou macOS)
**Para que** eu possa utilizar o sistema imediatamente sem necessidade de compilação

### Tarefas

1. Configurar pipeline de CI com GitHub Actions
   - Dado que um push é feito em qualquer branch
     Quando o pipeline de CI executa
     Então o código é compilado e todos os testes são executados automaticamente

2. Configurar pipeline de CD para geração de binários multiplataforma
   - Dado que uma tag `v*` é criada no repositório
     Quando o pipeline de CD executa
     Então os binários `assinatura-windows-amd64.exe`, `assinatura-linux-amd64` e `assinatura-darwin-amd64` são gerados via cross-compiling Go (`GOOS`/`GOARCH`)

3. Publicar binários no GitHub Releases com checksums
   - Dado que os binários foram gerados pelo pipeline
     Quando a release é publicada
     Então cada binário está disponível para download no GitHub Releases acompanhado do respectivo checksum SHA256

4. Adotar versionamento semântico (SemVer)
   - Dado que uma nova versão é lançada
     Quando a tag é criada
     Então segue o formato `vMAJOR.MINOR.PATCH` e o changelog da release é descritivo

---

## Resumo do Backlog

| User Story | Iteração prevista |
|------------|-------------------|
| US-01 — Invocar assinador.jar via CLI | Iteração 1 |
| US-02 — Simular assinatura com validação de parâmetros | Iterações 1 e 2 |
| US-03 — Gerenciar ciclo de vida do Simulador | Iteração 3 |
| US-04 — Provisionar JDK automaticamente | Iteração 3 |
| US-05 — Disponibilizar binários multiplataforma | Iteração 0, 1 e 4 |