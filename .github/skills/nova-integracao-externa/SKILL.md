---
name: nova-integracao-externa
description: Adiciona uma integração com sistema externo nos dois sentidos — SAÍDA (nós chamamos: classe concreta em adapter/, injetada no use case) e ENTRADA (ele nos avisa: entrypoint próprio, webhook/fila, com fato classificado e idempotência) — mantendo o domínio livre de frameworks. Use quando o sistema precisar chamar, consultar, notificar ou ser avisado por algo fora da aplicação.
---

# Nova integração externa (classe em `adapter/`, injetada no use case)

Este projeto **não** usa ports and adapters: não há interface no domínio. A integração é uma classe
concreta em `adapter/`, injetada pelo construtor no use case.
Modelos existentes:
- **Saída**: `adapter/notificacao/NotificadorBoleto` (`NotificarBoletoUC`) e `adapter/pagamentos/SistemaPagamentos`
  (`DevolverPagamentoBoletoUC`).
- **Entrada**: `entrypoint/webhook/v1/PagamentoWebhookResource` → `ReceberPagamentoBoletoUC`.

## 1. Classe da integração

`src/main/java/com/example/boleto/adapter/<sistema>/<Nome>.java`, `@ApplicationScoped`.

- Nome pelo que o negócio precisa (`NotificadorBoleto`, `ConsultaCliente`), não pela tecnologia (`SmtpClient`).
- **Fala a língua do domínio**: métodos públicos recebem/devolvem entidades e VOs de domínio.
  DTOs do sistema externo e a conversão ficam **dentro** do adapter.
- Erros técnicos (timeout, HTTP 5xx) viram exceção de domínio específica quando o chamador precisa
  reagir. Exceção de cliente HTTP/SDK nunca chega à application.
- Configuração (URLs, credenciais): `application.properties` + `@ConfigProperty` **no adapter**.
- Simulação: Javadoc dizendo o que seria na vida real; guarde o que foi "enviado" numa lista
  thread-safe para testes (modelo: `NotificadorBoleto.enviadas()`).
- **Não** crie interface para ela. Interface só se surgir uma segunda implementação real em produção.
- Nunca importe `application` nem `entrypoint`.

## 2. Use case

- Injete a classe pelo construtor.
- Ordem: domínio valida/transiciona → `repository.salvar(...)` → **depois** a chamada externa.
  Assim, conflito de concorrência ou regra violada não disparam efeito externo.
- Documente no Javadoc o risco restante ("enviou, mas o commit falhou") e que a solução de produção
  é o padrão **Outbox**, como em `NotificarBoletoUC`.
- Se a integração devolve dados para uma **regra**, a regra continua pura: o use case chama a
  integração e entrega o resultado no candidato (ver skill `nova-regra-de-negocio`, passo 5).

## 3. Testes

- Domínio continua testável sem a integração.
- `@QuarkusTest`: injete a classe (`@Inject NotificadorBoleto`) e verifique o que ela registrou.
  Para uma integração real, use `@InjectMock` (quarkus-junit-mockito) ou um perfil de teste.
- Garanta que o efeito externo **não** acontece quando o domínio recusa (409/422).

## 4. Integração de ENTRADA (o sistema externo nos avisa)

Modelo: `entrypoint/webhook/v1/PagamentoWebhookResource` + `ReceberPagamentoBoletoUC` + `Boleto.registrarPagamento`.

1. **Entrypoint próprio** (`entrypoint/webhook/v1`, `entrypoint/iib/v1`, consumidor de fila), separado
   da nossa API: outro cliente, outro contrato, outra autenticação. Continua fino: autentica, traduz,
   chama **um** use case.
2. **O contrato é DELE.** O `*Request` espelha o formato externo (centavos, data/hora com fuso, nomes
   dele) e traduz para o `*Input` na borda. O domínio nunca vê o formato externo.
3. **Autenticação na borda** (token/assinatura vindos de configuração; comparação em tempo constante).
   401 sai pelo `ApiExceptionHandler`.
4. **Fato, não comando.** Se o aviso descreve algo que já aconteceu (pagamento), o domínio não o recusa
   por regra: classifica com `ValidadorRegras.primeiraViolacao(...)` e vai para um estado de análise com
   o motivo. Só o **estado** pode recusar (409) quando o aviso não faz sentido (ex.: boleto já pago por
   outra transação).
5. **Idempotência.** O agregado guarda o id externo (`idTransacao`) e ignora o mesmo aviso repetido,
   respondendo 200 de novo.
6. **Identificação.** O sistema externo identifica pelo que ELE conhece (ex.: linha digitável): o
   repositório ganha `obterPor<Chave>(...)`, que lança a exceção `*NaoEncontrado` (404).
7. **Testes:** simule o sistema externo chamando o webhook no formato dele; cubra token inválido (401),
   chave desconhecida (404), formato inválido (400), aviso repetido (200 sem efeito) e o fato divergente.

## 5. Feche

- Adicione o pacote à tabela de camadas e ao "Mapa do projeto".
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
