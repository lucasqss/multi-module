---
name: layered-nova-integracao-externa
description: Adiciona integração com sistema externo (e-mail, SMS, fila, pagamentos, legado, mainframe) em projeto Java layered, nos dois sentidos — SAÍDA (nós chamamos: classe concreta em adapter/, injetada no use case, sem ports and adapters) e ENTRADA (ele nos avisa: entrypoint próprio, contrato dele, fato classificado e idempotência) — sem vazar framework para o domínio. Use quando o sistema precisar chamar, consultar, notificar ou ser avisado por algo fora da aplicação.
---

# Nova integração externa

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`): um
> processo de pagamento de boletos, que mostra o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

Primeiro descubra o **sentido**:

| Sentido | Quem chama | Onde fica | Exemplo |
|---|---|---|---|
| **Saída** | nós → sistema externo | classe concreta em `adapter/<sistema>/`, injetada no use case | notificar por e-mail, pedir devolução de pagamento |
| **Entrada** | sistema externo → nós | entrypoint próprio (`entrypoint/webhook/v1`, `entrypoint/iib/v1`, consumidor de fila) que chama **um** use case | aviso de pagamento, retorno de processamento |

O padrão **não** usa ports and adapters: não existe interface no domínio para nenhum dos dois.

## 0. Siga a convenção do projeto

Veja o **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou observe como os adapters e
entrypoints existentes estão organizados (normalmente um subpacote por sistema externo:
`adapter/<sigla-do-sistema>/`). Leia um existente antes de criar outro.

## SAÍDA (nós chamamos o sistema externo)

### 1. Classe da integração

`adapter/<sistema>/<Nome>.java` (`@ApplicationScoped` ou equivalente):

- Nome pelo que o negócio precisa (ilustração: `EmissorBoletos`, `BancoPagamentos`), não pela tecnologia
  (`SmtpClient`, `MciRestClient`).
- **Fala a língua do domínio**: métodos públicos recebem/devolvem entidades e VOs de domínio.
  DTOs do sistema externo e a conversão ficam **dentro** do adapter.
- Erros técnicos (timeout, 5xx, código de retorno do legado) → exceção de domínio específica quando
  o chamador precisa reagir. Exceção de SDK/cliente HTTP nunca chega à application.
- URLs/credenciais em configuração, lidas **no adapter**.
- **Não** crie interface. Só quando surgir uma segunda implementação real em produção.
- Nunca importe `application` nem `entrypoint`.

### 2. Use case

- Injete a classe pelo construtor.
- Ordem: domínio valida/transiciona → salvar → **depois** a chamada externa (conflito ou regra
  violada não disparam efeito externo).
- Documente o risco "chamou, mas o commit falhou"; em produção, padrão **Outbox**.
- Se o retorno alimenta uma **regra de negócio**, a regra continua pura: o use case chama a
  integração e entrega o resultado no candidato da spec (skill `layered-nova-regra-de-negocio`, passo 5).

## ENTRADA (o sistema externo nos avisa)

1. **Entrypoint próprio**, separado da nossa API: outro cliente, outro contrato, outra autenticação.
   Continua fino: autentica, traduz, chama **um** use case.
2. **O contrato é DELE.** O `*Request` espelha o formato externo (centavos, data/hora com fuso, nomes
   dele) e traduz para o `*Input` na borda. O domínio nunca vê o formato externo.
3. **Autenticação na borda** (token/assinatura de configuração; comparação em tempo constante → 401).
4. **Fato, não comando.** Aviso de algo que já aconteceu (pagamento, processamento concluído) não é
   recusado por regra: o domínio **classifica** com `ValidadorRegras.primeiraViolacao(...)` (as mesmas
   regras que recusariam um comando) e vai para um estado de análise com o motivo. Só o **estado** pode
   recusar (409) quando o aviso não faz sentido.
5. **Idempotência.** O agregado guarda o id externo e ignora o mesmo aviso repetido (responde 200 de novo):
   webhooks e filas entregam "pelo menos uma vez".
6. **Identificação** pelo que o sistema externo conhece (código de barras, protocolo): o repositório ganha
   `obterPor<Chave>(...)` que lança a exceção `*NaoEncontrado` (404).
7. Decisões humanas sobre o fato (aceitar, devolver, reprocessar) são **casos de uso próprios** na nossa API.

## Ilustração (processo de pagamento: dois sistemas externos, nos dois sentidos)

| Sistema | Sentido | Entrypoint / adapter | Use case |
|---|---|---|---|
| Emissor de boletos | Saída (consulta antes de decidir) | `adapter/emissor/EmissorBoletos` → `BoletoAPagar` | `SalvarBoletosDoProcessoUC` |
| Emissor de boletos | Entrada (boleto cancelado) | `entrypoint/webhook/v1/EmissorWebhookResource` | `ReceberCancelamentoBoletoUC` → cada processo decide (cancela ou análise) |
| Banco | Saída (depois de salvar) | `adapter/banco/BancoPagamentos` | `EnviarProcessoParaPagamentoUC`, `RetentarPagamentoUC` |
| Banco | Entrada (retorno) | `entrypoint/webhook/v1/BancoWebhookResource` + `RetornoBancoRequest` (centavos, data/hora com fuso) | `ReceberRetornoBancoUC` → `ProcessoPagamento.registrarRetornoBanco` (classifica com `CONFORMIDADE_RETORNO.primeiraViolacao`, idempotente pela tentativa) |

O BOLETO é dado de outro sistema: guardamos só uma cópia (`BoletoAPagar`) e o id dele; mudanças chegam como fato.

## Testes

- Domínio continua testável sem a integração.
- Saída: injete a classe (simulada) e verifique o que ela registrou; para uma integração real,
  substitua-a no teste (`@InjectMock` no Quarkus, `@MockBean` no Spring). Garanta que **nada** é enviado
  quando o domínio recusa (409/422).
- Entrada: simule o sistema externo chamando o entrypoint **no formato dele**; cubra autenticação (401),
  chave desconhecida (404), formato inválido (400), aviso repetido (200 sem efeito) e o fato divergente
  (vai para análise).

## Feche

Atualize o "Mapa do projeto", rode a skill `layered-revisao-arquitetura` e os testes.
