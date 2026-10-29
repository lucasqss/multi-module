# Exemplo: monólito de utilização do crédito

Esqueleto executável do desenho em [`../docs/arquitetura/README.md`](../docs/arquitetura/README.md): Java 21, Quarkus 3.38, Oracle, um deploy só, módulos com fronteira verificada no build.

Não é produção. Serve para mostrar **onde cada coisa mora** e provar, com testes, as regras que mais pesam: cota em um só processo ativo, fases em ordem, garantia antes do pagamento, quatro olhos, outbox com repetição segura, retorno idempotente e escrita no core só por procedure.

## Rodar

Pré-requisitos: JDK 21+, Maven, Docker (no Mac com Colima, ver abaixo).

```bash
mvn test
```

O teste sobe sozinho:
- **Oracle Free** (`gvenzl/oracle-free:23-slim-faststart`) via Testcontainers, com três usuários: `APP` (nosso schema, migrado pelo Flyway), `CORE` (simula o core: tabelas, a view `v_cota_credito` e a procedure `prc_informar_utilizacao`) e `CORE_INTEGRACAO` (com quem a aplicação lê a view e chama a procedure);
- **WireMock** com stubs do sistema de pagamentos (`POST /pagamentos/ordens`) e do GED (`POST /ged/documentos`).

Com **Colima**, exporte antes (ou grave em `~/.testcontainers.properties`):

```bash
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
```

## Organização

Quatro camadas no topo. Dentro do domínio, a partição é **por capacidade de negócio**, não por tipo técnico:

```
br.com.consorcio.utilizacao
├── rest/                      quem chama: organizado por PÚBLICO e JORNADA (muda com as telas)
│   ├── backoffice/
│   │   ├── abertura/          mapeia direto para as APIs (sem montador)
│   │   ├── analise/
│   │   ├── acompanhamento/    LinhaDoTempo → DTO da tela
│   │   ├── formalizacao/      FormalizacaoMontador: compõe processo + formalização + garantia
│   │   └── pagamento/
│   ├── consorciado/acompanhamento/   mesma LinhaDoTempo, outra forma de tela
│   ├── parceiro/
│   │   ├── pagamentos/        retorno do sistema de pagamentos (contrato estável)
│   │   └── contemplacao/      garantia usada pelo monólito de contemplação
│   └── erro/
├── dominio/
│   ├── nucleo/                o que vale para toda modalidade
│   │   ├── processo/          api/ · aplicacao/ · modelo/ (Processo e Objeto JOINED) · plugin/ (contrato do microkernel)
│   │   ├── partes/            pessoa, participação (papel), conta de recebimento
│   │   ├── credito/           vínculo cota-processo, recursos; porta ConsultaCore
│   │   ├── formalizacao/      instrumento genérico
│   │   ├── documento/         metadados, dono exclusivo; porta RepositorioArquivos
│   │   └── pagamento/         ordem, origens, retorno idempotente
│   ├── garantia/              ancorada na cota; não conhece processo
│   └── modalidade/            PLUG-INS: um pacote por modalidade
│       ├── imovel/            ProcessoImobiliario, ObjetoImovel, roteiro F0–F16, regra LAUDO_PENDENTE
│       └── servico/           exemplo de encaixe: sem alienação do objeto
├── integracao/                organizado por SISTEMA EXTERNO
│   ├── core/                  view (JDBC) + procedure
│   ├── pagamentos/            REST client + despachante da outbox
│   ├── ged/
│   └── outbox/                implementa compartilhado.saida.SaidaAssincrona
└── compartilhado/             exceções, conversores, porta SaidaAssincrona
```

Cada módulo do domínio tem as mesmas três pastas:

| Pasta | O que tem | Quem pode usar |
|---|---|---|
| `api/` | interface + records, na língua do negócio | qualquer um (rest, outros módulos) |
| `aplicacao/` | implementação da api: transação, orquestração | só o próprio módulo |
| `modelo/` | entidades JPA ricas (campos privados, métodos de negócio, construtor `protected`), repositórios Panache, portas | o próprio módulo e `integracao` (adaptadores implementam as portas) |

A entidade JPA **é** a entidade de domínio. Entre módulos a referência é por id; associação JPA só dentro do módulo. A única dependência de entidade entre módulos é a herança dos plug-ins (`ProcessoImobiliario extends Processo`).

### Jornada de negócio × jornada de tela

- **Jornada de negócio** (que fase vem depois, qual portão muda o estado): no roteiro do plug-in, em `modalidade/<x>/aplicacao`.
- **Jornada de tela** (o que aparece junto, em que ordem): em `rest/<público>/<jornada>`. Quando a tela junta vários módulos, um **montador** compõe; quando não junta, o controller mapeia direto.

Reorganizar uma tela mexe em `rest`. Mudar uma regra mexe em `modelo`. Mudar a jornada mexe no roteiro, numa versão nova.

### Migrações por dono

Cada módulo versiona as próprias tabelas, numa faixa própria:

| Pasta | Faixa | Dono |
|---|---|---|
| `db/migration/nucleo` | V1_00–V1_09 | módulos do núcleo (prefixos `pes_`, `prc_`, `cre_`, `ins_`, `doc_`, `pag_`) |
| `db/migration/garantia` | V1_10–V1_19 | garantia (`gar_`) |
| `db/migration/modalidade/imovel` | V1_20–V1_29 | plug-in imóvel (`imv_`) |
| `db/migration/modalidade/servico` | V1_30–V1_39 | plug-in serviço (`svc_`) |
| `db/migration/integracao` | V1_90–V1_99 | outbox (`int_`) |

### Fitness functions

`ArquiteturaTest` (ArchUnit) quebra o build se alguém:
- fizer `rest` usar `modelo`, `aplicacao`, `plugin` ou `integracao` (só `api`);
- depender de `rest`;
- fizer o domínio conhecer `integracao`, JDBC, REST client ou JAX-RS;
- fizer `compartilhado` conhecer qualquer outra camada;
- usar de fora o `modelo` ou a `aplicacao` de outro módulo;
- fizer o núcleo conhecer uma modalidade, ou uma modalidade conhecer outra;
- fizer a garantia depender do processo;
- criar ciclo entre módulos;
- declarar `@Entity` fora de `modelo` (ou da outbox).

## O fluxo testado

`FluxoImovelUrbanoTest` percorre a etapa 1 do roadmap (F0 a F16) pela camada REST, verificando:

| Passo | O que prova |
|---|---|
| Reserva da cota `1001-0042` | Lê a view do core; segundo processo com a mesma cota → `409 MOT-015` (índice único baseado em função) |
| `DOCUMENTOS` antes da abertura | `409 FASE_FORA_DE_ORDEM` |
| Documento | Vai ao GED (stub), guardamos só o id |
| Vistoria sem laudo | `409 LAUDO_PENDENTE` |
| Redação do instrumento | Cria a AF no módulo garantia (`PROPOSTA`); o painel de formalização (montador) junta instrumento, garantia e fases pendentes |
| Conferência sem registro | `409 GARANTIA_INSUFICIENTE`; depois de protocolo, registro e conferência → `APTO_PAGAMENTO` |
| Ordem | Origem = cota; quem preparou não aprova (`409 QUATRO_OLHOS`) |
| Pagamentos fora do ar | Mensagem fica na outbox, ordem segue `APROVADA`; na volta, reenvia com a mesma `Idempotency-Key` |
| Retorno `LIQUIDADO` repetido | Segundo é ignorado; processo `PAGO`, cota `UTILIZADA` |
| Informar utilização | Outbox chama a procedure; saldo no core cai de 300.000 para 20.000 |
| Encerramento | `CONCLUIDO`; canal do consorciado vê o processo |

`RoteiroTest` descobre os plug-ins varrendo `dominio.modalidade` e valida cada roteiro: dependências apontam para fases anteriores e os portões percorrem o estado macro em ordem válida até `CONCLUIDO`.

## Encaixar uma modalidade

Uma modalidade nova é um pacote novo em `dominio/modalidade/<nome>` e uma pasta nova de migração. Nada no núcleo muda:

1. `modelo/`: `ProcessoX extends Processo` e `ObjetoX extends Objeto`, com `@DiscriminatorValue` e tabela própria (subtabela JOINED);
2. `aplicacao/`: o roteiro (implementa `Roteiro`) e o registro `ModalidadeX implements Modalidade` (bean CDI, construtor sem argumentos);
3. regras só da modalidade: implemente `PreCondicaoFase` como classe comum (não CDI) e devolva em `regras()`. Assim ela não vale para as outras;
4. `api/` + `aplicacao/`: a abertura com os campos específicos, que entrega as entidades ao núcleo por `AberturaDeProcesso`;
5. `rest/backoffice/abertura`: o endpoint de abertura da nova modalidade.

`servico/` mostra o caso sem alienação: o portão para `APTO_PAGAMENTO` é a assinatura, e a regra `GarantiaSuficiente` vale mesmo assim, porque se liga ao **portão** e não ao nome da fase. `RoteiroTest` passa a verificar o roteiro novo sem ninguém registrar nada.

## Simplificações deliberadas

- Autenticação: o usuário vem no cabeçalho `X-Usuario` (backoffice) ou `X-Cpf` (consorciado). Na vida real, OIDC e autorização por perfil.
- Análises, pendências, sobra e seguros existem no design, mas aqui são só fases concluídas manualmente, sem entidade própria.
- AF proposta com cobertura = valor base do primeiro objeto, uma por cota.
- Upload de documento em JSON com base64, não multipart.
- O core está no mesmo container Oracle por economia. A integração continua pela outbox, como se fosse outro banco (ver pergunta 1 da seção 12 do design).
- Sem execução nem baixa de garantia, cessão, FGTS e pagamento por etapas.
