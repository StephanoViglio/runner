# Arquitetura do Assinador

Este documento descreve a arquitetura atual da aplicação `assinador`, um simulador de assinador digital que oferece duas operações principais (**assinatura** e **validação de assinatura**), acessíveis por dois canais (**HTTP/REST** e **CLI**).

A organização segue **Arquitetura Hexagonal** combinada com a regra de dependência da **Onion Architecture**. O domínio fica no centro, isolado de qualquer framework, e o mundo externo se comunica com ele através de **portas** (interfaces) implementadas por classes na camada de **presentation**.

## Sumário

- [Visão Geral](#visão-geral)
- [Diagrama da Arquitetura](#diagrama-da-arquitetura)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [As Quatro Camadas](#as-quatro-camadas)
  - [Domain](#1-domain)
  - [Application](#2-application)
  - [Presentation](#3-presentation)
  - [Infrastructure](#4-infrastructure)
- [Portas (Ports)](#portas-ports)
- [Regra de Dependência](#regra-de-dependência)

## Visão Geral

A aplicação está dividida em quatro camadas concêntricas:

| Camada | Papel | Conhece |
|---|---|---|
| `domain` | Vocabulário e regras de negócio | Nada externo |
| `application` | Orquestração dos casos de uso | `domain` |
| `presentation` | Tradução entre tecnologias externas e o domínio | `domain`, `application` |
| `infrastructure` | Configuração de frameworks e bootstrap | Todas as camadas |

A regra inviolável: **dependências apontam sempre para o centro**. Uma camada interna nunca importa de uma camada externa.

## Diagrama da Arquitetura

### 1. Visão em camadas concêntricas

```
        ┌───────────────────────────────────────────────┐
        │             INFRASTRUCTURE                    │
        │       (Spring, configs, entrypoints)          │
        │                                               │
        │   ┌───────────────────────────────────────┐   │
        │   │           PRESENTATION                │   │
        │   │   (REST, CLI, FakeSignatureProvider)  │   │
        │   │                                       │   │
        │   │   ┌───────────────────────────────┐   │   │
        │   │   │         APPLICATION           │   │   │
        │   │   │      (services / use cases)   │   │   │
        │   │   │                               │   │   │
        │   │   │   ┌───────────────────────┐   │   │   │
        │   │   │   │       DOMAIN          │   │   │   │
        │   │   │   │  model + ports + exc. │   │   │   │
        │   │   │   └───────────────────────┘   │   │   │
        │   │   └───────────────────────────────┘   │   │
        │   └───────────────────────────────────────┘   │
        └───────────────────────────────────────────────┘

        Dependências apontam para o centro (para dentro).
```

### 2. Visão hexagonal — portas e quem fala com quem

```
   ENTRADAS (driving)                              SAÍDAS (driven)

   ┌──────────────┐                               ┌──────────────────┐
   │  REST        │                               │  FakeSignature   │
   │  Controller  │                               │  Provider        │
   └──────┬───────┘                               └────────▲─────────┘
          │                                                │
          │ chama                                  implementa
          ▼                                                │
   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
   │   port.in       │───►│  Service        │───►│   port.out      │
   │  (interface)    │    │  (implementa    │    │  (interface)    │
   │                 │    │   port.in,      │    │                 │
   │ SignDocument    │    │   chama         │    │  Signature      │
   │ UseCase         │    │   port.out)     │    │  Provider       │
   │ VerifySignature │    │                 │    │                 │
   │ UseCase         │    │ SignDocument    │    │                 │
   └─────────────────┘    │ Service         │    └─────────────────┘
          ▲               │ VerifySignature │             ▲
          │               │ Service         │             │
          │ chama         └─────────────────┘     implementa
          │                                                │
   ┌──────┴───────┐                               ┌────────┴─────────┐
   │  CLI         │                               │  (futuras impls: │
   │  Commands    │                               │   PKCS#11)       │
   └──────────────┘                               └──────────────────┘
```

A leitura é direta: **componentes de entrada chamam port.in**; **o service implementa port.in e chama port.out**; **componentes de saída implementam port.out**. O service não conhece quem está dos dois lados — só conhece as portas.

## Estrutura de Pastas

```
com.runner.assinador/
│
├── domain/                              # Núcleo: regras e conceitos de negócio
│   ├── model/                           # Entidades e value objects
│   │   ├── BundleData.java
│   │   ├── ProvenanceData.java
│   │   ├── ResourceEntry.java
│   │   ├── CryptographicMaterial.java   # VO agrupando dados criptográficos
│   │   ├── CryptographicStrategy.java   # enum: SMARTCARD, TOKEN
│   │   ├── TimestampStrategy.java       # enum: IAT, TSA
│   │   ├── SignatureRequest.java        # entrada da port.out sign()
│   │   ├── VerificationRequest.java     # entrada da port.out verify()
│   │   ├── SignatureResult.java         # saída de sign()
│   │   └── VerificationResult.java      # saída de verify()
│   ├── exception/
│   │   ├── DomainErrorCode.java         # enum com códigos de erro do núcleo
│   │   └── SignatureException.java
│   └── port/
│       ├── in/                          # Portas de entrada (driving)
│       │   ├── SignDocumentUseCase.java
│       │   ├── VerifySignatureUseCase.java
│       │   ├── SignDocumentCommand.java
│       │   └── VerifySignatureCommand.java
│       └── out/                         # Portas de saída (driven)
│           └── SignatureProvider.java
│
├── application/                         # Orquestração dos casos de uso
│   └── service/
│       ├── SignDocumentService.java     # implementa SignDocumentUseCase
│       └── VerifySignatureService.java  # implementa VerifySignatureUseCase
│
├── presentation/                        # Fronteira com o mundo externo
│   ├── in/
│   │   ├── rest/                        # Entrada via HTTP
│   │   │   ├── SignatureController.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── dto/
│   │   │   │   ├── request/             # DTOs de entrada com @Valid
│   │   │   │   │   ├── SignRequestDTO.java
│   │   │   │   │   ├── VerifyRequestDTO.java
│   │   │   │   │   ├── BundleDTO.java
│   │   │   │   │   ├── ProvenanceDTO.java
│   │   │   │   │   ├── ResourceEntryDTO.java
│   │   │   │   │   └── CryptographicDTO.java
│   │   │   │   └── response/            # DTOs de saída (sucesso)
│   │   │   │       ├── SignResponseDTO.java
│   │   │   │       ├── SignatureCodingDTO.java
│   │   │   │       ├── SignatureIdentifierDTO.java
│   │   │   │       └── SignatureWhoDTO.java
│   │   │   └── mapper/
│   │   │       └── RestSignatureMapper.java
│   │   │
│   │   └── cli/                         # Entrada via terminal
│   │       ├── AssinadorCommand.java    # comando raiz picocli
│   │       ├── SignCommand.java
│   │       ├── ValidateCommand.java
│   │       ├── CliFileParser.java
│   │       ├── CliOutputFormatter.java
│   │       ├── input/
│   │       │   ├── SignInput.java
│   │       │   └── VerifyInput.java
│   │       └── mapper/
│   │           └── CliSignatureMapper.java
│   │
│   ├── out/
│   │   └── signature/                   # Saída para a operação de assinatura
│   │       ├── FakeSignatureProvider.java  # implementa SignatureProvider (simulação)
│   │       └── Pkcs11SignatureProvider.java # implementa SignatureProvider (real, via PKCS#11)
│   │
│   └── shared/                          # Código compartilhado entre componentes
│       ├── IssueSeverity.java
│       ├── IssueType.java
│       ├── OperationOutcomeCode.java
│       ├── fhir/                        # Modelos FHIR de OperationOutcome
│       │   ├── OperationOutcome.java
│       │   ├── Issue.java
│       │   ├── CodeableConcept.java
│       │   ├── Coding.java
│       │   └── OperationOutcomeFactory.java
│       └── signature/                   # Parsing/validação de envelope JWS, comum aos providers
│           ├── JwsEnvelope.java
│           ├── JwsEnvelopeParser.java
│           └── SignedContentDigest.java
│
└── infrastructure/                      # Configuração e bootstrap
    ├── configuration/
    │   ├── JacksonConfig.java
    │   └── Pkcs11Config.java            # registra o Provider SunPKCS11 (quando habilitado)
    ├── AssinadorApplication.java        # entrypoint Spring Boot
    └── AssinadorCli.java                # entrypoint da CLI
```

## As Quatro Camadas

### 1. Domain

Contém os conceitos e as regras de negócio do sistema, em Java puro, sem dependência de frameworks. É a camada mais isolada e a única que não pode importar de nenhuma outra.

**Conteúdo:**

- **`domain.model`** — modelos do negócio (`BundleData`, `ProvenanceData`, `ResourceEntry`, `CryptographicMaterial`, `SignatureRequest`, `VerificationRequest`, `SignatureResult`, `VerificationResult`) e enums (`CryptographicStrategy`, `TimestampStrategy`). Todos são imutáveis (campos `final`, sem setters) e validam suas próprias regras no construtor.
- **`domain.exception`** — `SignatureException` e o enum `DomainErrorCode` com o catálogo de erros do núcleo.
- **`domain.port.in`** — interfaces dos casos de uso (`SignDocumentUseCase`, `VerifySignatureUseCase`) e os commands que servem de entrada (`SignDocumentCommand`, `VerifySignatureCommand`). Os commands ficam aqui por fazerem parte do contrato da porta.
- **`domain.port.out`** — interfaces que o núcleo declara para depender do exterior. Hoje só existe `SignatureProvider`.

**Restrições:**

- Sem anotações de framework (`@Service`, `@Component`, `@Entity`, `@JsonProperty`, `@NotNull`, `@Pattern`)
- Sem imports de `application`, `presentation` ou `infrastructure`
- Validação feita no construtor com `if/throw`, não com Bean Validation

### 2. Application

Implementa os casos de uso declarados em `domain.port.in`. Cada caso de uso é orquestrado por um service que recebe o command, executa as validações que dependem de contexto externo, monta o request do domínio e delega o trabalho técnico para a porta de saída.

**Conteúdo:**

- **`application.service`** — uma classe por caso de uso (`SignDocumentService`, `VerifySignatureService`). Cada service implementa uma porta de `domain.port.in` e recebe a porta de saída pelo construtor.

**Responsabilidades do service:**

- Validações que dependem de contexto externo (ex.: janela de tolerância de timestamp em relação ao relógio do servidor)
- Tradução de command em request do domínio
- Chamada à porta de saída
- Retorno do resultado ao chamador

**Restrições:**

- Sem conhecimento de HTTP, JSON, JWS ou base64 — isso é responsabilidade de `presentation`
- Sem import de classes concretas que implementam portas de saída — o service depende apenas da interface
- Sem regras invariantes de modelos individuais — essas pertencem ao próprio modelo em `domain`

### 3. Presentation

Faz a tradução entre o mundo externo e o domínio. Comporta-se como um conjunto de plugues intercambiáveis: vários componentes podem se conectar à mesma porta sem afetar o núcleo.

Divide-se em três sub-pacotes:

**`presentation.in`** — componentes de entrada, que recebem chamadas externas e invocam portas de `domain.port.in`:

- `presentation.in.rest` — `SignatureController`, DTOs de request/response (com anotações Jakarta Validation), `RestSignatureMapper`, `GlobalExceptionHandler`. Sabe operar com Spring MVC.
- `presentation.in.cli` — comandos picocli (`SignCommand`, `ValidateCommand`, `AssinadorCommand`), `CliFileParser`, `CliOutputFormatter`, inputs e `CliSignatureMapper`. Sabe operar com terminal e arquivos JSON.

**`presentation.out`** — componentes de saída, que implementam portas de `domain.port.out`:

- `presentation.out.signature.FakeSignatureProvider` — implementa `SignatureProvider`. Constrói o JWS simulado no `sign()`, e no `verify()` parseia o JWS, valida sua estrutura (`alg`, `x5c`, `sigPId`) e recomputa o hash de integridade quando aplicável.
- `presentation.out.signature.Pkcs11SignatureProvider` — implementa `SignatureProvider` com assinatura criptográfica real via PKCS#11. Abre sessão no dispositivo (PIN + identificador vindos do `CryptographicMaterial` da requisição), assina o signing input do JWS (RFC 7515) com a chave do dispositivo e, no `verify()`, valida a assinatura criptograficamente contra o certificado em `x5c` (sem precisar do dispositivo, já que verificação usa apenas a chave pública). Ver [ADR-0001](adr/0001-localizacao-pkcs11-signature-provider.md) para a decisão de mantê-lo aqui e não em `infrastructure`.

**`presentation.shared`** — código compartilhado entre componentes de presentation que não pertence ao domínio:

- `presentation.shared.fhir` — modelos FHIR do `OperationOutcome` (`OperationOutcome`, `Issue`, `Coding`, `CodeableConcept`) e `OperationOutcomeFactory`, usados por REST e CLI para reportar erros num formato padronizado.
- `presentation.shared.signature` — `JwsEnvelopeParser`/`JwsEnvelope` (decodificação e validação estrutural do envelope JWS) e `SignedContentDigest` (resumo SHA-256 do conteúdo referenciado por `provenance.target`). Extraído para evitar duplicar essa lógica entre `FakeSignatureProvider` e `Pkcs11SignatureProvider` — ambos implementam a mesma porta `presentation.out`, mas não se conhecem entre si.
- `presentation.shared` — enums do catálogo de issues (`IssueSeverity`, `IssueType`, `OperationOutcomeCode`).

**Restrições:**

- Componentes de presentation não importam uns aos outros: REST não conhece CLI, nem vice-versa, e nenhum conhece `presentation.out` diretamente
- `presentation.shared` não importa de `presentation.in.*` nem de `presentation.out.*`
- Sem regras de negócio nem decisões de fluxo de caso de uso

### 4. Infrastructure

Concentra a configuração de frameworks e o bootstrap da aplicação. Existe em função das escolhas tecnológicas — uma troca de framework recairia majoritariamente sobre esta camada.

**Conteúdo:**

- **`infrastructure.configuration`** — classes `@Configuration` que registram beans do Spring: `JacksonConfig` provê o `ObjectMapper` consumido pelos providers de assinatura; `Pkcs11Config` registra o `Provider` SunPKCS11 da JVM (somente quando `assinador.signature-provider.type=pkcs11`), consumido por `Pkcs11SignatureProvider`.
- **Entrypoints** — `AssinadorApplication` (`@SpringBootApplication` com `main()` que sobe o servidor HTTP) e `AssinadorCli` (entrypoint que monta picocli sobre o contexto Spring para execução via terminal).

**Restrições:**

- Sem regras de negócio
- Sem implementação de portas (vai para `presentation`)
- Sem tradução entre formatos externos e domínio (também vai para `presentation`)

Infrastructure é a única camada autorizada a importar de todas as outras, pois é responsável por escolher implementações concretas para as portas e amarrar os componentes em runtime.

## Portas (Ports)

As portas são o coração do desenho. Existem dois tipos, em pacotes distintos do domínio.

### Portas de Entrada (`domain.port.in`)

São **interfaces que o núcleo expõe para o mundo**. Quem quer pedir algo ao núcleo monta o command correspondente e chama o método da porta.

| Porta | Command de entrada | Resultado |
|---|---|---|
| `SignDocumentUseCase` | `SignDocumentCommand` | `SignatureResult` |
| `VerifySignatureUseCase` | `VerifySignatureCommand` | `VerificationResult` |

Cada porta é **implementada por exatamente um service** em `application.service` e **chamada por todos os componentes de presentation de entrada** que precisam daquele caso de uso (hoje REST e CLI).

### Portas de Saída (`domain.port.out`)

São **interfaces que o núcleo declara para si mesmo**: "para realizar meu trabalho, eu vou precisar de algo que cumpra esse contrato". Quem cumpre é externo ao núcleo.

| Porta | Métodos | Implementada por |
|---|---|---|
| `SignatureProvider` | `sign(SignatureRequest)`, `verify(VerificationRequest)` | `FakeSignatureProvider` (simulação, padrão) ou `Pkcs11SignatureProvider` (real, via PKCS#11) |

A inversão é o ponto-chave: o núcleo **define a porta** mas **não conhece a implementação**. As duas implementações coexistem no código; qual delas fica ativa em runtime é decidido por uma única property, `assinador.signature-provider.type` (`fake` por padrão, `pkcs11` quando configurado — ver `Pkcs11Config`), usando `@ConditionalOnProperty` para que exatamente um bean de `SignatureProvider` exista no contexto Spring a cada execução. Nenhuma mudança no núcleo foi necessária para isso.

## Regra de Dependência

A regra que rege toda a arquitetura: **dependências apontam para o centro**.

```
infrastructure  →  presentation  →  application  →  domain
                                                       ↑
                                  (todas apontam para cá)
```

Cada camada só pode importar das camadas que estão à sua direita (mais ao centro), nunca das que estão à sua esquerda.

- **`domain`** não importa nada do projeto. É a camada mais isolada — só conhece a si mesma e o que vem da biblioteca padrão do Java.
- **`application`** importa de `domain`. Não importa de `presentation` nem de `infrastructure`.
- **`presentation`** importa de `domain` e, eventualmente, de `application` (via interfaces das portas, não classes concretas). Componentes de presentation não importam uns aos outros: REST não conhece CLI, e nenhum dos dois importa direto do `out`.
- **`infrastructure`** pode importar de todas as outras camadas. É a única com essa permissão, por ser responsável por amarrar as peças.

**Sinais de que algo está errado** (e que devem ser corrigidos quando aparecem):

- Algum arquivo em `domain` importando Spring, Jackson, Jakarta Validation ou qualquer coisa de framework
- `domain.port.in` importando `application` — a porta tem que ser autossuficiente
- `domain.port.out` importando `domain.port.in` — uma porta não conhece a outra
- `application` importando uma classe concreta de `presentation.out` — só pode conhecer a interface da porta
- `presentation.in.rest` importando `presentation.in.cli` (ou o contrário) — os dois lados de entrada são independentes
- `presentation.shared` importando algo de `presentation.in.*` ou `presentation.out.*` — o compartilhado não conhece os específicos