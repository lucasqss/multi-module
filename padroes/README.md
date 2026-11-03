# Padrões reutilizáveis — skills e instruções para o GitHub Copilot

Versão **genérica** das skills e instruções deste repositório, para usar em **qualquer** projeto
Java organizado em camadas (`domain` / `application` / `adapter` / `entrypoint` / `shared`) com
domínio rico, Specification, State e exceções específicas.

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
| `layered-novo-caso-de-uso` | Nova ação de ponta a ponta (REST ou mensageria) |
| `layered-nova-regra-de-negocio` | Nova validação de negócio (Specification) |
| `layered-novo-estado` | Novo status/transição (State) ou primeira máquina de estados |
| `layered-nova-integracao-externa` | Porta + adapter para sistema externo |
| `layered-revisao-arquitetura` | Revisão de camadas, domínio anêmico, SOLID, GRASP + script `verificar-camadas.sh` |

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

Depois, no projeto, peça ao Copilot (modo agente): **"use a skill layered-iniciar-projeto"**. Ela cria
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
