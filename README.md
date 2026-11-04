# financiamento — CRUD de referência

Referência de **CRUD com padrões de projeto e organização em camadas** (Quarkus 3, Java 25, Maven,
projeto único em `src/`). O exemplo é o **financiamento de imóvel e de veículo**, com exigências
**simplificadas e hipotéticas** (serve de modelo, não de sistema): o bem é avaliado por um sistema externo
(a **avaliação**) e a garantia é registrada por outro (o **cartório** ou o **órgão de trânsito**), que devolve
o resultado por webhook.

```
 AVALIAÇÃO (externo)            Nosso sistema                 ÓRGÃO DE REGISTRO (externo)
 ┌──────────────┐  consulta  ┌────────────────────┐   envio    ┌───────────────────────┐
 │ laudo /      │◀───────────│   Financiamento    │───────────▶│ cartório (imóvel) ou  │
 │ tabela       │            │ (nosso agregado)   │◀──webhook──│ trânsito (veículo)    │
 └──────────────┘            └────────────────────┘            └───────────────────────┘
```

## Camadas

```
entrypoint ──▶ application ──▶ domain          (ligadas por injeção de dependência de classes
                    │             ▲             concretas; sem ports and adapters)
                    └─▶ adapter ──┘
```

| Pacote (`com.example.financiamento`) | Papel |
|---|---|
| `domain` | `entity` (JPA), `repository` (Panache, lock pessimista), `valueobject`, `exception`, `specification`, `state` — framework só de persistência |
| `application/financiamento` | casos de uso (`*UC`), `comando/` (Command) e `dto/` (Input/Output) |
| `adapter` | só sistemas externos: `avaliacao/` e `registro/` (simulados) |
| `entrypoint` | `api/v1` (nossa API REST) e `webhook/v1` (entrada do órgão de registro) |
| `shared` | `Pagina`, `ClockProducer` |

## O agregado

**Um** agregado `Financiamento` e **um** fluxo para os dois tipos: as etapas são as mesmas; o que muda por
tipo são as regras, os documentos e com quem a etapa conversa (por quê: seção "Um componente ou um por tipo"
na skill). O rascunho (`EM_EDICAO`) é editado **por seções** — dados gerais (proponente, valor, prazo),
garantia (só a matrícula/placa; o resto vem da avaliação) e documentos — e só a submissão exige completude.

| Regra (exemplo) | Imóvel | Veículo |
|---|---|---|
| Prazo | 60 a 420 meses | 12 a 60 meses |
| Valor financiado | até 80% do valor avaliado | até 90% do valor avaliado |
| Documentos | identidade, comprovante de renda, **matrícula** | identidade, comprovante de renda, **CRLV** |
| Específica | — | veículo com até 10 anos |
| Registro da garantia | cartório de imóveis | órgão de trânsito (gravame) |

Comum aos dois: proponente, condições e garantia informados; parcela (simplificada: valor / prazo, sem juros)
de até 30% da renda; sem documento repetido.

```
EM_EDICAO ─submeter─▶ APROVADO ─contratar─▶ EM_REGISTRO ─registrado conforme─▶ CONTRATADO
   │                    │                       ├─ recusado ─▶ REGISTRO_RECUSADO ─reenviar─▶ EM_REGISTRO
   │                    │                       └─ registrado outro bem ─▶ EM_PENDENCIA
   └──── cancelar ──────┴──── (REGISTRO_RECUSADO e EM_PENDENCIA também) ───▶ CANCELADO
```

## Onde está cada padrão

Critério geral: **menos classes sem custo de entendimento** — DTO/mapper só entre entrypoint e application;
VO/record só com regra ou comportamento; o repositório já trata os filtros.

| Padrão | Onde |
|---|---|
| Domínio rico | `domain/entity/Financiamento` (a própria entidade JPA): sem setters; ordem **estado (409) → regras (422) → muda** |
| Value objects | `domain/valueobject`: `Dinheiro`, `Cpf`, `Proponente`, `Bem`, `DocumentosDoFinanciamento`, `IdProtocolo`... |
| Specification | `domain/specification`: base genérica; regras no catálogo `financiamento/RegrasFinanciamento` (uma linha cada; classe só quando parametrizada) |
| Chain of Responsibility | `CadeiaValidacao` + `RegrasFinanciamento.SUBMISSAO`: um elo comum e um elo por tipo |
| State | `domain/state/StatusFinanciamento`: enum com o comportamento de cada status |
| Command | `application/financiamento/comando`: submeter, contratar, reenviar registro, cancelar; invocador `ExecutarComandoFinanciamentoUC` |
| Exceções por família | `domain/exception`: 400 / 404 / 409 / 422, mapeadas por `ApiExceptionHandler` |
| Comando × fato | retorno do registro não é recusado: é **classificado** (`EM_PENDENCIA` se registrou outro bem); idempotente via `IdProtocolo` (aviso repetido ou antigo = 200) |
| Efeito após o commit | o envio ao órgão só sai se a transação confirmou (`ExecutarComandoFinanciamentoUC.aposCommit`) |
| Lock pessimista | `FinanciamentoRepository.obterParaAlteracao` (`SELECT ... FOR UPDATE`); bloqueio → 409 `RECURSO_BLOQUEADO` |

## API

| Método e caminho | Caso de uso |
|---|---|
| `POST /api/v1/financiamentos` `{"tipo":"IMOVEL"}` | `CriarFinanciamentoUC` |
| `GET /api/v1/financiamentos?tipo=&status=&pagina=&tamanho=` | `ListarFinanciamentosUC` |
| `GET /api/v1/financiamentos/{id}` | `ConsultarFinanciamentoUC` |
| `PUT /api/v1/financiamentos/{id}` (proponente, valor, prazo) | `AlterarDadosFinanciamentoUC` |
| `PUT /api/v1/financiamentos/{id}/garantia` `{"identificacao":"500"}` | `InformarGarantiaUC` (consulta a avaliação) |
| `PUT /api/v1/financiamentos/{id}/documentos` | `SalvarDocumentosUC` |
| `DELETE /api/v1/financiamentos/{id}` | `ExcluirFinanciamentoUC` (só rascunho) |
| `POST /api/v1/financiamentos/{id}/submissao` · `/contratacao` · `/reenvio-registro` · `/cancelamento` | `ExecutarComandoFinanciamentoUC` + comando |
| `POST /webhooks/v1/registro/retornos` (header `X-Webhook-Token`) | `ReceberRetornoRegistroUC` |

Erros sempre no formato `{"codigo": "...", "mensagem": "...", "campos": [...]}`.

A avaliação é simulada: **imóvel** = matrícula (só dígitos), valor = matrícula × R$ 1.000 (matrícula `500` →
R$ 500.000); **veículo** = placa Mercosul, valor R$ 100.000 e ano-modelo = 2000 + os 2 últimos dígitos
(`ABC1D23` → 2023). Matrícula começando com `0` ou placa com `ZZZ` = bem desconhecido.

## Rodar

Banco: **Oracle** em todos os profiles. Em dev e test, o Quarkus Dev Services sobe sozinho um Oracle Free em
container (precisa de **Docker**; imagem `ghcr.io/gvenzl/oracle-free`, versão fixa no `application.properties`).
Para usar um Oracle existente, defina `QUARKUS_DATASOURCE_JDBC_URL`, `..._USERNAME` e `..._PASSWORD`.
Em produção: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (e o esquema por migração, não pelo Hibernate).

```bash
mvn quarkus:dev   # http://localhost:8081 (sobe o Oracle em container)
mvn test          # domínio (JUnit puro) + repositório/transação + ponta a ponta (@QuarkusTest, Oracle)
mvn verify        # + integração: o jar empacotado contra o Oracle (@QuarkusIntegrationTest, FinanciamentoResourceIT)
```

## Copilot e reuso em outros projetos

O padrão está numa skill única e genérica, [`.github/skills/layered-crud`](.github/skills/layered-crud/SKILL.md):
regras de camadas, entidade rica, caso de uso e Command, Specification + Chain, State, exceções,
integração externa, lock, testes e checklist de revisão — com este repositório só como ilustração.
A pasta traz também `templates/` (base genérica para iniciar um projeto), `mapa-do-projeto.md` e
`verificar-camadas.sh`. Para usar em outro projeto:

```bash
cp -r .github/skills/layered-crud <outro-projeto>/.github/skills/
# no outro projeto: peça ao Copilot "inicie o projeto no padrão da skill layered-crud"
bash .github/skills/layered-crud/verificar-camadas.sh
```
