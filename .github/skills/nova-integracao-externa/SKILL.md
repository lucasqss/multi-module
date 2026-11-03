---
name: nova-integracao-externa
description: Adiciona uma integração com sistema externo (e-mail, SMS, fila, outro serviço, banco registrador) como porta em domain.gateway e adapter em adapter/, mantendo o domínio livre de frameworks. Use quando o sistema precisar chamar, consultar ou notificar algo fora da aplicação.
---

# Nova integração externa (porta + adapter)

Modelo existente: porta `domain/gateway/NotificadorBoleto` + adapter `adapter/notificacao/NotificadorBoletoFake`,
usados por `application/boleto/NotificarBoletoUC`.

## 1. Porta no domínio

`src/main/java/com/example/boleto/domain/gateway/<Capacidade>.java`: interface pequena (ISP),
nomeada pelo que o **domínio precisa** (`NotificadorBoleto`), não pela tecnologia (`SmtpClient`).
Parâmetros e retornos são tipos de **domínio**. Sem anotações.

- Persistência não é gateway: vai em `domain/repository`.
- Se a integração devolve dados para uma **regra**, a regra continua pura: o use case chama o
  gateway e entrega o resultado no candidato (ver skill `nova-regra-de-negocio`, passo 5).

## 2. Adapter

`src/main/java/com/example/boleto/adapter/<sistema>/<Nome>.java`, `@ApplicationScoped`, implementando a porta.

- Converte tipos de domínio ↔ DTOs do sistema externo **dentro do adapter**.
- Traduz erros técnicos (timeout, HTTP 5xx) para uma exceção de domínio específica, se o chamador
  precisar reagir. Nunca deixe exceção de cliente HTTP/SDK vazar para application.
- Simulação: sufixo `Fake` + Javadoc dizendo o que seria na vida real. Guarde o que foi "enviado"
  numa lista thread-safe para testes (modelo: `NotificadorBoletoFake.enviadas()`).
- Configuração (URLs, credenciais): `application.properties` + `@ConfigProperty` **no adapter**.

## 3. Use case

- Injete a **porta** (interface), nunca a classe do adapter.
- Ordem: domínio valida/transiciona → `repository.salvar(...)` → **depois** a chamada externa.
  Assim, conflito de concorrência ou regra violada não disparam efeito externo.
- Documente no Javadoc o risco restante ("enviou, mas o commit falhou") e que a solução de produção
  é o padrão **Outbox**, como em `NotificarBoletoUC`.

## 4. Testes

- Domínio continua testável sem o adapter.
- `@QuarkusTest`: injete o fake (`@Inject NotificadorBoletoFake`) e verifique o que ele registrou.
- Garanta que o efeito externo **não** acontece quando o domínio recusa (409/422).

## 5. Feche

- Adicione o pacote do adapter à tabela de camadas do `README.md`.
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
