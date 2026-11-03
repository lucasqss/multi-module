---
name: layered-nova-integracao-externa
description: Adiciona integração com sistema externo (e-mail, SMS, fila, outro serviço, legado, mainframe) em projeto Java layered, como porta no domínio e adapter dedicado, sem vazar framework para o domínio. Use quando o sistema precisar chamar, consultar ou notificar algo fora da aplicação.
---

# Nova integração externa (porta + adapter)

## 0. Siga a convenção do projeto

Veja o **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou observe:
- **Onde ficam as portas de integração?** Em `domain/gateway` ou junto das de persistência em
  `domain/repository`. Use o que o projeto já usa; se não houver nenhuma, prefira `domain/gateway`.
- **Como os adapters são organizados?** Normalmente um subpacote por sistema externo
  (`adapter/<sigla-do-sistema>/`). Leia um adapter existente antes de criar outro.

## 1. Porta no domínio

Interface pequena (ISP), nomeada pelo que o **domínio precisa** (`NotificadorContrato`,
`ConsultaCliente`), não pela tecnologia (`SmtpClient`, `MciRestClient`). Parâmetros e retornos com
tipos de domínio. Sem anotações.

Se o retorno alimenta uma **regra de negócio**, a regra continua pura: o use case chama a porta e
entrega o resultado no candidato da spec (skill `layered-nova-regra-de-negocio`, passo 5).

## 2. Adapter

`adapter/<sistema>/<Nome>.java` implementando a porta (`@ApplicationScoped` ou equivalente):

- DTOs do sistema externo e conversão domínio ↔ externo **dentro do adapter**.
- Erros técnicos (timeout, 5xx, código de retorno do legado) → exceção de domínio específica quando
  o chamador precisa reagir. Exceção de SDK/cliente HTTP nunca chega à application.
- URLs/credenciais em configuração, lidas **no adapter**.
- Simulação/teste: implementação `*Fake` documentada, guardando chamadas numa lista thread-safe.

## 3. Use case

- Injete a **porta**, nunca a classe do adapter.
- Ordem: domínio valida/transiciona → salvar → **depois** a chamada externa (conflito ou regra
  violada não disparam efeito externo).
- Documente o risco "chamou, mas o commit falhou"; em produção, padrão **Outbox**.

## 4. Testes

- Domínio continua testável sem o adapter.
- Teste de integração com o `*Fake` injetado: verifique o que foi registrado, e que **nada** é
  enviado quando o domínio recusa (409/422).

## 5. Feche

Documente o adapter, rode a skill `layered-revisao-arquitetura` e os testes.
