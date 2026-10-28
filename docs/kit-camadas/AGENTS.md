# AGENTS.md — como trabalhar neste repositório

<!-- MODELO do kit-camadas. Na Fase 1 da migração, o agente troca todos os <marcadores> pelos nomes reais do projeto
     e apaga este comentário. Na Fase 6, apaga a seção "Migração em andamento". -->

Instruções para quem gera ou altera código aqui, seja pessoa ou modelo de IA. São regras, não sugestões: a maioria é
verificada pelo `ArquiteturaTest` e quebra o build.

- O porquê de cada decisão está em `docs/adr/`.
- A explicação completa do modelo, com os modelos de código, está em `docs/kit-camadas/GUIA.md` e
  `docs/kit-camadas/MODELOS.md`.

## Migração em andamento

Este projeto está sendo migrado para o modelo em camadas. Siga `docs/kit-camadas/GUIA.md`, seção 6:

- **uma fase por vez**;
- `mvn verify` verde e um commit ao fim de cada fase;
- **nunca** editar os ITs da Fase 1 nem mudar o contrato da API.

Fase atual: **<fase>**. O diagnóstico está em `docs/migracao/diagnostico.md`.

## O projeto em poucas linhas

- Quarkus <versão>, Java <versão>, Oracle (Flyway) e **um módulo Maven**, organizado em **camadas por pacote**.
- <O que o sistema faz, em uma frase.>
- <Se houver um fluxo com vários tipos: "Os <tipos> passam pelo mesmo motor (`<Nucleo>Service`); o que muda por
  tipo fica numa implementação de `<PontoDeExtensao>`.">
- <Estados, se houver: `A → B → C`.>
- Entradas: <REST, fila…>. Saídas: <sistemas externos>, na camada de integração.

## Camadas

Pacote raiz `<pacote-base>`:

| Camada | Contém | Pode usar | Nunca usa |
|---|---|---|---|
| `apresentacao` | `rest/` e `mensageria/`, cada um com `dto/`; `TratadorDeErros` | `negocio`, `persistencia.consulta`, `modelo` | `persistencia.repositorio`, `integracao`, outro canal, JPA, transação |
| `negocio` | `<nucleo>/` (motor, ponto de extensão, regras comuns), uma pasta por tipo, uma pasta por cadastro | `integracao`, `persistencia`, `modelo` | `apresentacao`; um tipo não usa outro; JPA |
| `integracao` | uma pasta por sistema externo: `*Client`, `*ClientSimulado`, formato externo, `*Integracao` | `modelo` | as outras camadas; um sistema não usa outro; JPA |
| `persistencia` | `repositorio/` (gravar e ler entidades; entidades técnicas) e `consulta/` (projeções para as telas) | `modelo` | as outras camadas |
| `modelo` | entidades JPA, enums de estado, value objects, exceções comuns | JPA (só anotações) | qualquer camada; REST, CDI, Quarkus |

**Gravar** passa sempre pelo negócio, por causa das regras e da transação. **Ler** vai da apresentação direto a
`persistencia.consulta`, sem um serviço que só repassaria.

## Regras invioláveis (garantidas pelo `ArquiteturaTest`)

- Dependências da tabela acima; toda classe numa camada; sem ciclos.
- A apresentação não grava direto: lê só de `persistencia.consulta`.
- Pastas do negócio não se enxergam; o núcleo não conhece os tipos.
- Sistemas externos não se enxergam; canais de entrada não se enxergam.
- HTTP só na apresentação e na integração.
- JPA e Hibernate só em `modelo` e `persistencia`; `@Entity` só nelas; `@Repository` só na persistência.
- A API não devolve entidade, nem dentro de `List` ou `Optional`.
- Transação (`@Transactional`, `QuarkusTransaction`) só no negócio.
- Nomes pelo papel: `*Resource`, `*Client`, `*Integracao`, `*Service` e `<PontoDeExtensao>*`.
- Interface só com mais de uma implementação real: o ponto de extensão e os clientes HTTP. Garantido por revisão.

## Onde coloco…?

| Preciso de… | Vai em |
|---|---|
| Regra que vale para todo tipo | `negocio/<nucleo>/RegrasComuns` |
| Regra de um tipo | `negocio/<tipo>/Regras<Tipo>` (`Especificacao`) |
| Regra que consulta o banco | na implementação do ponto de extensão, por último, usando um repository |
| Valor com validação (CPF, valor…) | record em `modelo`, validando no construtor |
| Estado ou transição nova | o enum de estado em `modelo` + o CHECK numa migration nova |
| Erro de negócio novo | exceção no `negocio/<pacote>` (ou no `modelo`, se a integração também lança) + método no `TratadorDeErros` |
| Chamada a sistema externo | `integracao/<sistema>`: `*Client` + `*ClientSimulado` + `<Sistema>Integracao` |
| Endpoint HTTP | `apresentacao/rest` + request (`paraModelo()`) e response (`de(...)`) em `rest/dto` |
| Consumo de fila | `apresentacao/mensageria` + mensagem em `mensageria/dto` |
| Consulta ou listagem para tela | `persistencia/consulta` (record plano + `select new`) |
| Cadastro simples (CRUD) | entidade em `modelo` + repository + `negocio/<nome>/<Nome>Service` + resource |
| Tabela ou coluna | entidade em `modelo` + migration nova `V<n>__*.sql` |
| Configuração de sistema externo | `application.properties`, com URL por variável de ambiente e timeouts de 2 s |

Exemplos completos de cada item: `docs/kit-camadas/MODELOS.md`.

## Convenções

- **Entidades são o modelo:**
  - têm construtor de pedido (sem id) para a apresentação;
  - têm um construtor sem argumentos `protected`, só para o Hibernate;
  - getters e métodos de evento, sem setters.
- **Records** para value objects, DTOs e projeções. Conversão como método do DTO; não há mapper.
- **Validação:**
  - value objects validam no construtor;
  - regras com `Especificacao`, que devolve o motivo;
  - campos ausentes chegam nulos para a regra recusar com a mensagem certa.
- **Mensagens de exceção de negócio** são para o usuário final: viram o `detail` do Problem Details.
- **Transações:**
  - no fluxo, curtas e explícitas (`QuarkusTransaction.requiringNew()`), com chamada externa sempre fora;
  - em CRUD, `@Transactional` no serviço;
  - efeito externo (PDF, notificação) depois do commit.
- **Concorrência:** troca de estado por compare-and-set (`update ... where ESTADO = :de`); conflito → 409.
- **Erros HTTP:** `application/problem+json` com `type` fixo (`/problemas/...`).
  - 422 regra;
  - 404 não encontrado;
  - 409 conflito;
  - 400 parâmetro;
  - 503 sistema externo.
- **Comentários:** o Javadoc explica o porquê e o contrato, não repete o código.

## Não faça

- Interface com uma única implementação, mapper entre entidade e domínio, ou serviço que só repassa leitura.
- Regra de negócio em resource, consumidor ou DTO.
- Gravar pela apresentação; devolver entidade na API.
- JPA fora de `modelo`/`persistencia`; tabela só técnica no `modelo`.
- Transação em volta de chamada a sistema externo.
- `amqp-host`, URL do banco ou credenciais no `application.properties`: desliga o Dev Services e vaza ambiente.
- Editar migration já aplicada.
- Pular, desabilitar ou afrouxar teste para o build passar.

## Comandos

```bash
mvn test -DexcludedGroups=banco   # testes puros + ArchUnit, sem Docker
mvn verify                        # tudo: @QuarkusTest com Oracle (Dev Services), ITs contra o jar, cobertura
mvn quarkus:dev                   # dev; sistemas externos simulados
```

Todo `@QuarkusTest` leva `@Tag("banco")` e usa o mesmo conjunto de `@WithTestResource` dos outros.

## Definição de pronto

- [ ] `mvn verify` verde, sem teste pulado ou afrouxado.
- [ ] Comportamento novo tem teste:
  - puro para regra, estado e value object;
  - `@QuarkusTest` para o que passa pelo banco;
  - IT para o que passa por HTTP.
- [ ] Decisão nova ou mudada → ADR em `docs/adr/`.
- [ ] Regra, receita ou armadilha mudou → este arquivo.

## Armadilhas

As do modelo estão em `docs/kit-camadas/GUIA.md`, seção 8. As próprias deste projeto vêm a seguir:

- <armadilhas encontradas no diagnóstico e na migração>
