# 0011. Entradas por canal em `adapter/inbound`

- **Status:** Substituída por [0014](0014-camadas-num-modulo-so.md) (os canais continuam separados, em `apresentacao/<canal>`)
- **Data:** 2026-10-27

## Contexto

Um processo pode ser iniciado por mais de um canal: API REST, fila (ex.: vinda do mainframe), agendamento. Sem uma
regra, cada canal tende a reimplementar validação e fluxo, ou a regra de negócio passa a depender de detalhes do
canal (HTTP, broker). Fila ainda traz dois problemas que o HTTP não tem: reentrega da mesma mensagem e erro sem
alguém esperando a resposta.

## Decisão

- Toda forma de iniciar algo é uma borda de entrada em `processo-app/.../adapter/inbound/<canal>` (`rest/`,
  `messaging/`, `scheduler/`), com o `dto/` do canal. Ela só traduz e chama o `MotorDeProcessamento`; as regras são
  as mesmas para todos os canais. Um canal não usa as classes de outro (ArchUnit).
- Canal com dependência pesada (cliente IBM MQ/JMS, código gerado de copybook) ou descartável vira módulo de entrada
  próprio (ex.: `entrada-mainframe`), o espelho do `integracoes`: depende do core, o app depende dele.
- Fila:
  - **idempotência obrigatória:** o id da mensagem é a chave de `motor.criar(solicitacao, chave)`; reentrega devolve
    o processo já criado;
  - **destino pelo tipo do erro:** regra violada ou mensagem malformada → responde `recusado` e confirma (reentregar
    não muda o resultado); falha temporária (sistema externo, banco) → nack para reentrega;
  - **resposta à origem** fica no próprio adapter de entrada (é a mesma conversa); aviso a outro sistema por
    iniciativa do negócio é saída, em `integracoes`, atrás de um port.
- Canal fora do HTTP ativa o request context (`@ActivateRequestContext`).

## Consequências

- Canal novo não toca no core; o motor continua o único ponto de entrada.
- O exemplo é AMQP 1.0 (`quarkus-messaging-amqp`): Artemis pelo Dev Services no dev e nos testes, e o mesmo código
  serve contra o IBM MQ por um canal AMQP. Trocar de broker é trocar connector e configuração.
- Cada fila consumida custa uma tabela de chaves (`PROCESSO_IDEMPOTENCIA`) e testes de ack/nack.
