---
name: layered-enxugar-estrutura
description: Enxuga a estrutura de pastas de um projeto Java layered organizado por tipo (elimina domain/factory, model, enums, repository, gateway, service, shared/utils, shared/config) e deixa coesa a interação entre classes (UC não chama UC, sem *Service/*Utils genéricos), movendo cada classe para o lugar certo sem mudar comportamento. Use quando pedirem para reorganizar, enxugar, simplificar pastas, reduzir acoplamento ou "arrumar a estrutura" do projeto.
---

# Enxugar a estrutura (por tipo) e deixar a interação coesa

Objetivo: manter a organização **por tipo de classe**, mas só com pastas que têm papel próprio.
Pastas que existem por causa de abstrações desnecessárias são **eliminadas**: o conteúdo vai para
onde o papel dele já mora (ou some, se a abstração não precisava existir).

## Estrutura alvo

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
│   └── <feature>       *UC (+ dto/ com *Input/*Output)
├── adapter
│   ├── database        <Agregado>Repository, *JpaEntity, *Mapper, Filtro*
│   └── <sistema>       uma pasta por sistema externo (ex.: aco, mci, sgd)
├── entrypoint
│   ├── api/v1          *Resource, request/, response/, handler/
│   └── iib/v1          consumidores de mensageria (se houver)
└── shared              Pagina, ClockProducer (sem subpastas)
```

## Passo 1 — Inventário (não mova nada ainda)

```bash
find src/main/java -type d | sed 's#src/main/java/##' | while read d; do
  printf "%3d  %s\n" "$(find "src/main/java/$d" -maxdepth 1 -name '*.java' | wc -l)" "$d"; done
```

Monte uma tabela **classe → destino** usando o passo 2 e mostre ao usuário **antes** de mover.
Classes cujo destino seja ambíguo: pergunte.

## Passo 2 — Destino de cada pasta eliminada

| Pasta | Pergunta para cada classe | Destino |
|---|---|---|
| `domain/factory` | A factory só monta objetos (sem dependências injetadas)? | **Some**: vira método estático da entidade (`Contrato.criar(...)`) ou do candidato (`SolicitacaoContrato.paraCriacao(..., clock, ...)`). |
| | Ela injeta repositórios/serviços para montar algo? | É orquestração: vira parte do **use case**. |
| `domain/model` | Tem identidade e ciclo de vida? | `entity` |
| | É imutável e definido pelos valores? | `valueobject` (como `record` que valida) |
| | Só existe como entrada das regras? | `specification/<agregado>` (candidato) |
| | Só transporta dados entre camadas/sistemas? | `application/<feature>/dto` ou dentro de `adapter/<sistema>` |
| `domain/enums` | É o status/situação de uma entidade com transições? | `state` (junto dos estados; vira State pattern se houver `if`/`switch` nele) |
| | É usado por um único VO? | **Some**: vira método do VO (ex.: `TipoDocumento` → `documento.pessoaFisica()`) |
| | Demais enums de valor | `valueobject` |
| `domain/repository`, `domain/gateway` | Interface com uma única implementação? | **Some** a interface; a implementação concreta fica em `adapter/database` ou `adapter/<sistema>`, injetada direto no UC. |
| | Repositório Panache/JPA dentro de `domain`? | `adapter/database` (domínio não depende de framework). |
| `domain/service` | Método que lê/altera uma única entidade? | Método de negócio **na entidade** (Information Expert). |
| | Método que só valida? | **Spec** no catálogo de regras. |
| | Método que coordena repositórios/integrações? | **Use case**. |
| | Lógica pura entre vários agregados, sem dono claro? | Único caso para manter um serviço de domínio (Java puro). Se sobrar só ele, avalie com o usuário. |
| `shared/utils`, `shared/config` | Função sobre um conceito do domínio (formatar CPF, somar dinheiro)? | Método do **VO** correspondente. |
| | Genérica de verdade (paginação, `Clock`)? | `shared/` direto, sem subpastas. |

## Passo 3 — Interação coesa (quem chama quem)

```
Resource/Consumer ─▶ UC ─┬─▶ Repository / integração (adapter) ─▶ Mapper ─▶ Entidade.reconstituir
                         ├─▶ Solicitacao<Agregado>.para<Momento>(...)   (monta o candidato)
                         └─▶ Entidade.<metodoDeNegocio>(...) ─┬─▶ Estado (autoriza a ação)
                                                              └─▶ Regras<Agregado> ─▶ *Spec ─▶ candidato
```

Procure e corrija:
- **UC chamando outro UC** → extraia a lógica comum para a entidade, um VO ou uma spec; cada UC chama o domínio.
- **Resource/consumer acessando repositório, entidade ou adapter** → passe pelo UC.
- **Entidade chamando repositório** → o UC busca o fato e entrega à entidade.
- **`if`/`switch` em status fora de `state`** → State pattern (skill `layered-novo-estado`).
- **Regra de negócio em UC/service** → spec (skill `layered-nova-regra-de-negocio`).
- **Classe `*Utils`/`*Helper`/`*Service` genérica** → cada método vai para o dono do dado.

## Passo 4 — Executar com segurança (refatoração, sem mudar comportamento)

1. Garanta testes verdes **antes** de começar (`mvn test`).
2. **Uma pasta por vez**, na ordem: `shared/*` → `domain/enums` → `domain/model` → `domain/factory`
   → `domain/repository`/`gateway` → `domain/service` → correções de interação.
3. Mova com `git mv` (preserva histórico) e ajuste `package`/imports; remova imports do mesmo pacote.
4. Ao eliminar uma abstração (factory, interface), troque os usos e apague a classe.
5. Depois de cada pasta: compile, rode os testes e o `verificar-camadas.sh` da skill
   `layered-revisao-arquitetura`; faça um commit por pasta (fácil de revisar e de reverter).
6. **Não** misture com mudança de regra de negócio, renomeação de endpoints ou contratos de API.
7. Ao final, atualize o **"Mapa do projeto"** e a documentação de estrutura.

## Resultado esperado

Relate: pastas antes → depois (contagem), classes eliminadas (abstrações removidas), classes movidas
e correções de interação feitas, cada uma com o motivo.
