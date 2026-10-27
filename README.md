# Processo — motor único por segmento em Maven multi-module (Quarkus 3.33 · Java 25 · Oracle)

Exemplo de **processos de dois tipos (imobiliário e veicular) com um motor único**, e de um **cadastro simples
(agências)** pelo caminho curto. Mostra:

- **Motor único** para todos os tipos: o mesmo fluxo de criação e o mesmo ciclo de vida. O veicular entrou sem mudar
  o motor.
- **Máquina de estados com o padrão State** num enum: `ABERTO → EM_PROCESSAMENTO → CONCLUIDO | RECUSADO` e
  `ABERTO → CANCELADO`.
- **Processo de cada tipo como filha do processo**: herança JPA `JOINED`.
- **Escrita e leitura separadas**: `processo-core` e `processo-query` não se enxergam.
- **Entradas por canal** (REST e fila AMQP com idempotência) e **sistemas externos atrás de ports** (cadastro, PDF).
- **CRUD sem cerimônia** quando não há fluxo: agências, com serviço + repository.

## Módulos

```
                          ┌───────────────────────────┐
                          │       processo-app        │  entradas (REST, fila), montagem,
                          │  inbound · ITs · ArchUnit │  ITs e regras entre módulos
                          └─────────────┬─────────────┘
              ┌─────────────────────────┼──────────────────────┐
     ┌────────▼───────┐       ┌─────────▼──────┐      ┌────────▼─────────┐
     │  integracoes   │       │ processo-core  │      │  processo-query  │
     │ implementa     ├──────►│ motor, segmento│      │ projeções,       │
     │ ports do core  │       │ cadastro.*     │      │ somente leitura  │
     │ (cadastro, pdf)│       │ sem HTTP       │      │ não vê o core    │
     └────────────────┘       └────────┬───────┘      └────────┬─────────┘
                                       └───────────┬───────────┘
                                         ┌─────────▼─────────┐
                                         │     jpa-core      │  @Entity e Flyway
                                         └───────────────────┘
```

| Módulo | Responsabilidade |
|---|---|
| `jpa-core` | O schema: entidades e migrations. Nenhuma regra. |
| `processo-core` | Toda a regra, sem HTTP: o `motor`, os `segmento.*` e os cadastros simples (`cadastro.agencia`). |
| `processo-query` | Consultas somente leitura de processos, com projeções. |
| `integracoes` | Um pacote por sistema externo (`cadastro`, `pdf`), implementando ports do core. |
| `processo-app` | As entradas, uma pasta por canal em `adapter/inbound` (`rest/`, `messaging/`), a montagem do jar, os ITs e o ArchUnit entre módulos. |

## API

| Entrada | O que faz |
|---|---|
| `POST /processos/imobiliarios`, `POST /processos/veiculares` | cria pelo motor; `201 {"id", "numero"}` |
| `POST /processos/{id}/processamento` · `POST /processos/{id}/cancelamento` | processa (`200 {"situacao", "motivo"}`) ou cancela (`204`), para qualquer tipo |
| `GET /processos/imobiliarios/{id}`, `GET /processos/veiculares/{id}` | detalhe do processo |
| `GET /processos?situacao=ABERTO&pagina=1&tamanho=20` | listagem paginada de todos os tipos: `{"itens", "pagina", "tamanho", "total"}` |
| fila `processos.imobiliarios.novos` (AMQP) | cria processo por mensagem (o `id` é a chave de idempotência); responde em `processos.imobiliarios.respostas` |
| `POST/GET /agencias`, `GET/PUT/DELETE /agencias/{codigo}` | CRUD de agências |
| `GET /q/openapi`, `GET /q/health` | contrato da API (Swagger UI em `/q/swagger-ui` no dev) e health |

Erros em Problem Details (`application/problem+json`, com `type`, `title`, `status` e `detail`): `422` regra
violada ou transição inválida, `409` conflito de concorrência, `404` não encontrado, `400` parâmetro inválido,
`503` sistema externo fora do ar.

## Como rodar

Pré-requisitos: Java 25, Maven 3.9+, Docker.

```bash
# Testes unitários (sem Docker)
mvn -pl processo-core,integracoes -am test

# Tudo: contratos no Oracle, teste da fila e ITs (Dev Services sobe Oracle e Artemis no Docker)
mvn verify

# Modo dev: Oracle e Artemis via Dev Services, cadastro e PDF simulados; Swagger UI em http://localhost:8080/q/swagger-ui
mvn install -DskipTests && mvn -pl processo-app quarkus:dev

# Jar empacotado contra o Oracle do docker-compose
docker compose up -d --wait
QUARKUS_DATASOURCE_JDBC_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1 \
QUARKUS_DATASOURCE_USERNAME=processo QUARKUS_DATASOURCE_PASSWORD=processo \
SISTEMA_PDF_URL=http://localhost:8089 CADASTRO_URL=http://localhost:8090 \
AMQP_HOST=localhost AMQP_PORT=5672 AMQP_USERNAME=artemis AMQP_PASSWORD=artemis \
java -jar processo-app/target/quarkus-app/quarkus-run.jar
```

## Para quem chega

| Você é… | Comece por |
|---|---|
| Pessoa nova na equipe | [`docs/guia.html`](docs/guia.html) (abra no navegador): seções "Comece por aqui" (ordem de leitura do código), "Glossário" e "Onde coloco…?". Depois, **como** está organizado: módulos, fluxos, modelo de dados, integrações, testes, receitas e armadilhas. |
| Modelo de IA, ou quem vai mudar código | [`AGENTS.md`](AGENTS.md): regras invioláveis (e o teste que garante cada uma), onde colocar cada coisa, receitas em checklist, o que não fazer e a definição de pronto. O `CLAUDE.md` importa o mesmo arquivo. |
| Quem quer entender uma decisão | [`docs/adr/`](docs/adr/README.md): **por que** cada decisão, uma por arquivo. Decisão nova = ADR nova, a partir do [modelo](docs/adr/0000-modelo.md). |

Cada pacote principal também tem um `package-info.java` com o papel dele e o que pode ou não depender.
