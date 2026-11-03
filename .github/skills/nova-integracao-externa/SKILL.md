---
name: nova-integracao-externa
description: Adiciona uma integração com sistema externo (e-mail, SMS, fila, outro serviço, banco registrador) como classe concreta em adapter/, injetada diretamente no use case, mantendo o domínio livre de frameworks. Use quando o sistema precisar chamar, consultar ou notificar algo fora da aplicação.
---

# Nova integração externa (classe em `adapter/`, injetada no use case)

Este projeto **não** usa ports and adapters: não há interface no domínio. A integração é uma classe
concreta em `adapter/`, injetada pelo construtor no use case.
Modelo existente: `adapter/notificacao/NotificadorBoleto`, usado por `application/boleto/NotificarBoletoUC`.

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

## 4. Feche

- Adicione o pacote à tabela de camadas e ao "Mapa do projeto".
- `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
