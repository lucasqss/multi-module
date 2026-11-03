---
name: layered-nova-integracao-externa
description: Adiciona integração com sistema externo (e-mail, SMS, fila, outro serviço, legado, mainframe) em projeto Java layered, como classe concreta em adapter/ injetada diretamente no use case (sem ports and adapters), sem vazar framework para o domínio. Use quando o sistema precisar chamar, consultar ou notificar algo fora da aplicação.
---

# Nova integração externa (classe em `adapter/`, injetada no use case)

O padrão **não** usa ports and adapters: não existe interface no domínio. A integração é uma classe
concreta em `adapter/`, injetada pelo construtor no use case.

## 0. Siga a convenção do projeto

Veja o **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou observe como os adapters
existentes estão organizados (normalmente um subpacote por sistema externo: `adapter/<sigla-do-sistema>/`).
Leia um adapter existente antes de criar outro.

## 1. Classe da integração

`adapter/<sistema>/<Nome>.java` (`@ApplicationScoped` ou equivalente):

- Nome pelo que o negócio precisa (`NotificadorContrato`, `ConsultaCliente`), não pela tecnologia
  (`SmtpClient`, `MciRestClient`).
- **Fala a língua do domínio**: métodos públicos recebem/devolvem entidades e VOs de domínio.
  DTOs do sistema externo e a conversão domínio ↔ externo ficam **dentro** do adapter.
- Erros técnicos (timeout, 5xx, código de retorno do legado) → exceção de domínio específica quando
  o chamador precisa reagir. Exceção de SDK/cliente HTTP nunca chega à application.
- URLs/credenciais em configuração, lidas **no adapter**.
- Simulação: Javadoc dizendo o que seria na vida real; guarde as chamadas numa lista thread-safe.
- **Não** crie interface. Só quando surgir uma segunda implementação real em produção.
- Nunca importe `application` nem `entrypoint`.

## 2. Use case

- Injete a classe pelo construtor.
- Ordem: domínio valida/transiciona → salvar → **depois** a chamada externa (conflito ou regra
  violada não disparam efeito externo).
- Documente o risco "chamou, mas o commit falhou"; em produção, padrão **Outbox**.
- Se o retorno alimenta uma **regra de negócio**, a regra continua pura: o use case chama a
  integração e entrega o resultado no candidato da spec (skill `layered-nova-regra-de-negocio`, passo 5).

## 3. Testes

- Domínio continua testável sem a integração.
- Teste de integração: injete a classe (simulada) e verifique o que ela registrou; para uma
  integração real, substitua-a no teste (`@InjectMock` no Quarkus, `@MockBean` no Spring).
- Garanta que **nada** é enviado quando o domínio recusa (409/422).

## 4. Feche

Atualize o "Mapa do projeto", rode a skill `layered-revisao-arquitetura` e os testes.
