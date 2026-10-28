# Processo — motor único por modalidade, em camadas num módulo só (Quarkus 3.33 · Java 25 · Oracle)

Exemplo de **processos de crédito de consórcio de duas modalidades (imobiliária e veicular) com um motor único**, e de
um **cadastro simples (agências)** pelo caminho curto. Mostra:

- **Camadas num módulo só**: apresentação, negócio, integração, persistência e o modelo que todas usam, com as
  fronteiras garantidas por teste (ArchUnit).
- **Motor único** para todas as modalidades: o mesmo fluxo de criação e o mesmo ciclo de vida. A veicular entrou sem
  mudar o motor.
- **Máquina de estados com o padrão State** num enum: `ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO` e
  `ABERTO → CANCELADO`.
- **Processo de cada modalidade como filha do processo**: herança JPA `JOINED`; as entidades são o modelo.
- **Leitura direta**: as telas leem projeções da persistência, sem passar pelo negócio.
- **Entradas por canal** (REST e fila AMQP com idempotência) e **sistemas externos isolados** na integração (cadastro,
  PDF).
- **CRUD sem cerimônia** quando não há fluxo: agências, com serviço + repository.

## Camadas

```
            ┌──────────────────────────────────────────────┐
            │ apresentacao   rest/ · mensageria/            │  traduz pedido ↔ modelo; sem regra
            └───────┬──────────────────────────────┬────────┘
              grava │                              │ lê
            ┌───────▼──────────────────┐           │
            │ negocio                  │           │
            │ processo/ (o motor)      │           │
            │ imobiliario/ veicular/   │           │
            │ agencia/                 │           │
            └───┬──────────────────┬───┘           │
     ┌──────────▼───────┐   ┌──────▼───────────────▼──────┐
     │ integracao       │   │ persistencia                │
     │ cadastro/ pdf/   │   │ repositorio/   consulta/    │
     └──────────────────┘   └─────────────────────────────┘
       modelo: entidades, Situacao, Cpf, Valor — usado por todas as camadas, não usa nenhuma
```

| Camada | Responsabilidade |
|---|---|
| `apresentacao` | Por onde os pedidos chegam: uma pasta por canal (`rest/`, `mensageria/`), cada uma com o seu `dto/`. Só traduz. |
| `negocio` | A lógica do sistema: o motor (`ProcessoService`), uma pasta por modalidade e os cadastros simples. |
| `integracao` | A lógica de sistemas externos: uma pasta por sistema (`cadastro/`, `pdf/`), com o cliente HTTP e a tradução de respostas e erros. |
| `persistencia` | A lógica de banco: `repositorio/` grava e lê entidades; `consulta/` projeta para as telas. |
| `modelo` | O vocabulário comum: entidades JPA, a `Situacao` (State), `Cpf` e `Valor`. |

## API

| Entrada | O que faz |
|---|---|
| `POST /processos/imobiliarios`, `POST /processos/veiculares` | cria pelo motor; `201 {"id", "numero"}` |
| `POST /processos/{id}/processamento` · `POST /processos/{id}/cancelamento` | processa (`200 {"situacao", "motivo"}`) ou cancela (`204`), para qualquer modalidade |
| `GET /processos/imobiliarios/{id}`, `GET /processos/veiculares/{id}` | detalhe do processo |
| `GET /processos?situacao=ABERTO&pagina=1&tamanho=20` | listagem paginada de todas as modalidades: `{"itens", "pagina", "tamanho", "total"}` |
| fila `processos.imobiliarios.novos` (AMQP) | cria processo por mensagem (o `id` é a chave de idempotência); responde em `processos.imobiliarios.respostas` |
| `POST/GET /agencias`, `GET/PUT/DELETE /agencias/{codigo}` | CRUD de agências |
| `GET /q/openapi`, `GET /q/health` | contrato da API (Swagger UI em `/q/swagger-ui` no dev) e health |

Erros em Problem Details (`application/problem+json`, com `type`, `title`, `status` e `detail`): `422` regra
violada ou transição inválida, `409` conflito de concorrência, `404` não encontrado, `400` parâmetro inválido,
`503` sistema externo fora do ar.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes puros: regras, estados, integrações e ArchUnit (sem Docker)
mvn test -Prapido

# Tudo: testes com Oracle, teste da fila e ITs (Dev Services sobe Oracle e Artemis no Docker)
mvn verify

# Modo dev: Oracle e Artemis via Dev Services, cadastro e PDF simulados; Swagger UI em http://localhost:8080/q/swagger-ui
mvn quarkus:dev

# Jar empacotado contra o Oracle do docker-compose (o broker AMQP vem de fora, pelas variáveis AMQP_*)
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=processo QUARKUS_DATASOURCE_PASSWORD=processo \
SISTEMA_PDF_URL=http://localhost:8089 CADASTRO_URL=http://localhost:8090 \
AMQP_HOST=localhost AMQP_PORT=5672 AMQP_USERNAME=artemis AMQP_PASSWORD=artemis \
java -jar target/quarkus-app/quarkus-run.jar
```

## Para quem chega

| Você é… | Comece por |
|---|---|
| Pessoa nova na equipe | [`docs/guia.html`](docs/guia.html) (abra no navegador): o que o sistema faz, como as peças se encaixam e por onde começar a ler o código, em linguagem simples (uns 10 minutos). |
| Modelo de IA, ou quem vai mudar código | [`AGENTS.md`](AGENTS.md): as camadas, as regras invioláveis (e o teste que garante cada uma), onde colocar cada coisa, receitas em checklist, o que não fazer e a definição de pronto. O `CLAUDE.md` importa o mesmo arquivo. |
| Vai aplicar este modelo em outro projeto (com Copilot ou outro agente) | [`docs/kit-camadas/`](docs/kit-camadas/README.md): guia completo das decisões, modelos de código, ArchUnit com modo de migração, `AGENTS.md` modelo e os prompts fase a fase. |
| Quem quer entender uma decisão | [`docs/adr/`](docs/adr/README.md): **por que** cada decisão, uma por arquivo. Decisão nova = ADR nova, a partir do [modelo](docs/adr/0000-modelo.md). |

Cada camada também tem um `package-info.java` com o papel dela e o que pode ou não usar.
