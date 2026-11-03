# Padrões reutilizáveis — skills e instruções para o GitHub Copilot

Versão **genérica** das skills e instruções deste repositório, para usar em **qualquer** projeto
Java organizado em camadas (`domain` / `application` / `adapter` / `entrypoint` / `shared`) com
domínio rico, Specification, State e exceções específicas. As camadas se ligam por **injeção de
dependência de classes concretas** (sem ports and adapters): use cases injetam repositórios e
integrações de `adapter/`; o domínio não depende de nenhuma outra camada nem de framework.

O projeto `pagamento-boleto` (este repositório) é a **implementação de referência**: as skills
específicas dele ficam em `.github/skills/`; as genéricas, aqui.

## Conteúdo

| Pasta | O que é |
|---|---|
| `skills/layered-*` | Skills genéricas (prefixo `layered-` para não colidir com skills de projeto) |
| `skills/layered-iniciar-projeto/templates/` | Base genérica em Java (`*.java.tmpl`, pacote `__PACOTE__`) |
| `skills/layered-iniciar-projeto/mapa-do-projeto.md` | Modelo da seção "Mapa do projeto" |
| `instructions/*.instructions.md` | Regras por camada (`applyTo` por caminho), para copiar em cada projeto |

| Skill | Use para |
|---|---|
| `layered-iniciar-projeto` | Criar a base genérica num projeto novo/existente e o "Mapa do projeto" |
| `layered-novo-agregado-crud` | Novo cadastro/entidade com CRUD completo |
| `layered-novo-caso-de-uso` | Decidir se é um caso de uso (critério acima) e criar a ação de ponta a ponta |
| `layered-nova-regra-de-negocio` | Nova validação de negócio (Specification) |
| `layered-novo-estado` | Novo status/transição (State) ou primeira máquina de estados |
| `layered-nova-integracao-externa` | Integração com sistema externo (classe em `adapter/`) |
| `layered-enxugar-estrutura` | Eliminar pastas desnecessárias (`factory`, `model`, `enums`, `repository`, `service`, `shared/*`) e corrigir quem chama quem |
| `layered-revisao-arquitetura` | Revisão de camadas, domínio anêmico, SOLID, GRASP + script `verificar-camadas.sh` |

## Estrutura de pastas que as skills seguem (por tipo, enxuta)

```
<pacote-base>
├── domain
│   ├── entity          entidades (aggregate roots)
│   ├── valueobject     value objects (records) e enums de valor
│   ├── exception       famílias + exceções específicas
│   ├── specification   base genérica (Specification, RegraNegocio, ValidadorRegras)
│   │   └── <agregado>  specs, catálogo Regras<Agregado> e candidatos (Solicitacao<Agregado>...)
│   └── state           Estado<Agregado>, estados concretos e enum Status<Agregado>
├── application
│   └── <agregado>      *UC (+ dto/ com *Input/*Output)
├── adapter
│   ├── database        <Agregado>Repository, *JpaEntity, *Mapper, Filtro*
│   └── <sistema>       uma pasta por sistema externo
├── entrypoint
│   └── api/v1          *Resource, request/, response/, handler/ (outros: iib/v1...)
└── shared              Pagina, ClockProducer (sem subpastas)
```

**Não crie** estas pastas (cada uma tem um destino melhor):

| Pasta | Para onde vai o conteúdo |
|---|---|
| `domain/factory` | Criação = método estático da entidade (`Contrato.criar`) ou de um record de contexto (`CondicoesFinalizacao.de(clock, ...)`) |
| `domain/model` | Candidato das regras → `specification/<agregado>`; DTO → `application/.../dto` ou `adapter/<sistema>` |
| `domain/enums` | Status → `state`; enum de valor → `valueobject`; enum usado por um só VO → método do VO |
| `domain/repository`, `domain/gateway` | Repositório/integração concretos em `adapter/` (sem ports) |
| `domain/service` | Lógica de uma entidade → na entidade; regra → spec; orquestração → use case |
| `shared/utils`, `shared/config` | `shared/` direto; função de um conceito → método do VO |

## Interação entre classes que as skills seguem (quem chama quem)

```
Resource/Consumer ─▶ UC ─┬─▶ Repository / integração (adapter) ─▶ Mapper ─▶ Entidade.reconstituir
                         ├─▶ Solicitacao<Agregado>.para<Momento>(...)   (monta o candidato)
                         └─▶ Entidade.<metodoDeNegocio>(...) ─┬─▶ Estado (autoriza a ação)
                                                              └─▶ Regras<Agregado> ─▶ *Spec ─▶ candidato
```

- Cada classe conversa só com a camada **imediatamente** abaixo dela nesse fluxo.
- Resource não toca repositório nem entidade; **UC não chama outro UC**, nem spec, nem estado.
- Entidade nunca chama repositório; spec só lê o candidato; estado só conhece estados.
- Lógica reaproveitada entre UCs mora na entidade, num VO ou numa spec, nunca num UC compartilhado
  nem numa classe `*Service`/`*Utils` genérica.

## O que é um caso de uso (critério)

**Um caso de uso = uma intenção do usuário (ou de outro sistema) com significado de negócio**,
executada numa transação. Teste rápido: dá para dizer "o <ator> quer <verbo> <algo>" e a tela ou o
sistema dispara isso como **uma** ação? Então é um caso de uso.

- **Uma classe, um `executar`, uma transação.** Nunca um UC com vários métodos públicos: isso é um
  `*Service` disfarçado (vários motivos para mudar, dependências acumuladas, transações misturadas).
- **Não é um por entidade.** Partes de um agregado (itens, destinatários, parcelas, documentos) não têm
  UC nem repositório próprios: o UC carrega o agregado, chama o método dele e salva o agregado inteiro.
- **Não é um por operação técnica** (incluir/alterar/remover item). Seção que a tela edita como um
  bloco → **um UC por seção**, com `PUT` substituindo a seção inteira.
- **Separe** em UCs diferentes quando muda: a regra, a permissão, a transição de estado ou o ator/gatilho
  (usuário × banco × job). **Junte** quando sempre acontecem juntos, com a mesma regra e o mesmo gatilho.

| Situação | Quantos casos de uso | Exemplo |
|---|---|---|
| Criar o agregado | 1 — nasce em `EM_EDICAO` se pode ficar incompleto | `CriarContratoUC` |
| Editar uma seção/parte editada como bloco | 1 por seção (`PUT` substitui a seção) | `SalvarParcelasContratoUC`, `SalvarBeneficiariosContratoUC` |
| Incluir/remover uma parte que é ação de negócio própria (regras, permissão ou evento próprios) | 1 por ação | `AnexarDocumentoUC` |
| Finalizar a edição (exige completude) | 1 | `FinalizarEdicaoContratoUC` |
| Transição de estado / evento de negócio | 1 por transição | `AprovarContratoUC`, `CancelarContratoUC` |
| Consultar | 1 por consulta | `ConsultarContratoUC`, `ListarContratosUC` |
| Entidade com vida própria, usada por vários agregados | CRUD próprio; aqui só o id | `Pagador` cadastrado à parte |

### Agregado editado por partes (padrão `EM_EDICAO`)

1. `CriarContratoUC` cria o rascunho em `EM_EDICAO` só com o mínimo.
2. Um `Salvar<Secao>ContratoUC` por seção; cada um valida **só a seção** (regras de seção no catálogo).
   O agregado pode continuar incompleto.
3. `FinalizarEdicaoContratoUC` exige a **completude** (conjunto de regras de finalização no catálogo, sobre o agregado
   inteiro) e muda o estado. Só a partir daí o agregado "vale" (é cobrável, visível, enviado...).
4. O estado `EM_EDICAO` permite editar/excluir; os seguintes não (`validarEdicao()` → 409).
5. `@Version` no agregado: duas edições concorrentes, mesmo de seções diferentes, não se sobrescrevem.

## Como as skills se adaptam a cada projeto

As skills falam de **papéis** (catálogo de regras, base de estados, candidato das regras, builder de
testes...), não de classes do boleto. Para saber quem cumpre cada papel, elas:

1. leem a seção **"Mapa do projeto"** do `.github/copilot-instructions.md` (ou `AGENTS.md`);
2. se não houver mapa, procuram no código (`extends RegraNegocio<`, `abstract sealed class Estado`...)
   e **copiam a forma** das classes existentes;
3. se a base não existir, mandam usar `layered-iniciar-projeto` antes.

Por isso, o **mapa é o único trecho que muda de projeto para projeto**. Exemplo preenchido: a seção
"Mapa do projeto" de `.github/copilot-instructions.md` deste repositório.

## Instalação

### Skills: uma vez por máquina (valem para todos os projetos)

```bash
# Linux/macOS (Git Bash no Windows)
mkdir -p ~/.copilot/skills
cp -r padroes/skills/layered-* ~/.copilot/skills/
```

`~/.agents/skills` também é aceito. Para versionar junto com um projeto específico, copie para
`.github/skills/` desse projeto em vez da pasta pessoal.

### Instruções: uma vez por projeto

```bash
mkdir -p <projeto>/.github/instructions
cp padroes/instructions/*.instructions.md <projeto>/.github/instructions/
```

Depois, no projeto, peça ao Copilot (modo agente): **"use a skill layered-iniciar-projeto"**.
Em projeto que já existe com muitas pastas, peça antes **"use a skill layered-enxugar-estrutura"**:
ela mostra o inventário e o destino de cada classe antes de mover, e migra uma pasta por commit. Ela cria
a base que faltar e o `copilot-instructions.md` com as regras e o mapa. Revise o mapa gerado.

### Verificação de camadas

```bash
bash ~/.copilot/skills/layered-revisao-arquitetura/verificar-camadas.sh          # descobre o pacote base
bash ~/.copilot/skills/layered-revisao-arquitetura/verificar-camadas.sh src/main/java/br/com/empresa/sistema
```

Sai com código 1 se houver violação (dá para usar em pipeline de CI) e 2 se não achar o pacote base.

## Onde funciona

Skills: modo agente no VS Code, Visual Studio e JetBrains (conforme a versão do Copilot), Copilot CLI,
agente de nuvem e revisão de PR no GitHub. Instruções por caminho (`applyTo`): IDEs, agente de nuvem
e revisão de PR.

## Manutenção

Ao evoluir o padrão no projeto de referência, replique aqui:
- mudou uma classe base (`Specification`, exceções, `Dinheiro`, handler...)? Regere o template
  correspondente trocando o pacote por `__PACOTE__`;
- mudou uma regra de arquitetura? Atualize a skill `layered-*` e a instrução genérica equivalentes.
