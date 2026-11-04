---
name: layered-novo-caso-de-uso
description: Decide se algo é um caso de uso (uma intenção do usuário, um executar, uma transação; partes de agregado passam pelo agregado; um UC por seção editada; completude só na finalização) e cria a ação de ponta a ponta em projeto Java layered (entidade, use case, DTOs, endpoint e testes). Use quando pedirem uma nova operação, endpoint, comando, consulta, consumo de mensagem, ou perguntarem quantos casos de uso criar.
---

# Novo caso de uso (de ponta a ponta)

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`), que mostra
> o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

Construa **de dentro para fora**: domínio → application → entrypoint → testes.

## 0. Isto é UM caso de uso? (decida antes de codificar)

**Um caso de uso = uma intenção do usuário (ou de outro sistema) com significado de negócio**,
executada numa transação. Teste rápido: dá para dizer "o <ator> quer <verbo> <algo>" e a tela ou o
sistema dispara isso como **uma** ação? Então é um caso de uso.

- **Uma classe, um `executar`, uma transação.** Nunca um UC com vários métodos públicos: isso é um
  `*Service` disfarçado (vários motivos para mudar, dependências acumuladas, transações misturadas).
- **Não é um por entidade.** Partes de um agregado (itens, destinatários, parcelas, documentos) não têm
  UC nem repositório próprios: o UC carrega o agregado, chama o método dele e salva o agregado inteiro.
- **Não é um por operação técnica** (incluir/alterar/remover item). Seção que a tela edita como um
  bloco → **um UC por seção**, com `PUT` substituindo a seção inteira.
- **Separe** em UCs diferentes quando muda: a regra, a permissão, a transição de estado ou o ator/gatilho
  (usuário × banco × job). **Junte** quando sempre acontecem juntos, com a mesma regra e o mesmo gatilho.

| Situação | Quantos casos de uso | Ilustração (boleto) |
|---|---|---|
| Criar o agregado | 1 — nasce em `EM_EDICAO` se pode ficar incompleto | `CriarBoletoUC` |
| Editar uma seção/parte editada como bloco | 1 por seção (`PUT` substitui a seção) | `SalvarItensBoletoUC`, `SalvarDestinatariosBoletoUC` |
| Incluir/remover uma parte que é ação de negócio própria (regras, permissão ou evento próprios) | 1 por ação | (não há no boleto; ex.: `AnexarComprovanteUC`) |
| Finalizar a edição (exige completude) | 1 | `EmitirBoletoUC` |
| Aviso de **sistema externo** (fato: pagamento, retorno de processamento) | 1, chamado por um entrypoint de integração (webhook/fila) | `ReceberPagamentoBoletoUC` (webhook) |
| Transição de estado / evento de negócio | 1 por transição | `NotificarBoletoUC`, `CancelarBoletoUC`, `AceitarPagamentoBoletoUC` |
| Consultar | 1 por consulta | `ConsultarBoletoUC`, `ListarBoletosUC` |
| Entidade com vida própria, usada por vários agregados | CRUD próprio; aqui só o id | (no boleto o pagador é VO; num cadastro de clientes, seria agregado próprio) |

#### Comando × fato

- **Comando** = alguém pede algo ("emita", "cancele"). Pode ser recusado: regra violada → 422.
- **Fato** = algo que já aconteceu lá fora ("o pagador pagou R$ 499"). **Não** se recusa: o domínio
  registra e **classifica**. As mesmas regras servem: `ValidadorRegras.primeiraViolacao(...)` não lança,
  devolve a regra não atendida, que vira o motivo de um estado de análise.
- Fatos vindos de fora chegam repetidos (webhook/fila entregam "pelo menos uma vez"): o agregado ignora
  o mesmo id externo (**idempotência**).

#### Agregado editado por partes (padrão `EM_EDICAO`)

1. `Criar<Agregado>UC` cria o rascunho em `EM_EDICAO` só com o mínimo.
2. Um `Salvar<Secao><Agregado>UC` por seção; cada um valida **só a seção** (regras de seção no catálogo).
   O agregado pode continuar incompleto.
3. O UC de finalização exige a **completude** (conjunto de regras de finalização no catálogo, sobre o agregado
   inteiro) e muda o estado. Só a partir daí o agregado "vale" (é cobrável, visível, enviado...).
4. O estado `EM_EDICAO` permite editar/excluir; os seguintes não (`validarEdicao()` → 409).
5. `@Version` no agregado: duas edições concorrentes, mesmo de seções diferentes, não se sobrescrevem.

Ilustração: `CriarBoletoUC` → `SalvarItensBoletoUC` / `SalvarDestinatariosBoletoUC` / `AlterarBoletoUC` → `EmitirBoletoUC`.

Nunca acrescente um segundo método público a um UC existente: crie outro UC.

## 0.1 Localize os modelos

Pelo **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, ou buscando: classes `*UC` em
`application/`, resources/consumers em `entrypoint/`, `*Request`/`*Response`, `*Input`/`*Output`.
**Abra um use case parecido e copie a forma** (nomes, injeção, anotações, retorno).

## 1. Domínio (comportamento primeiro)

- A **decisão** é da entidade: método com nome de negócio. Status importa → estado
  (skill `layered-novo-estado`); regra sobre dados → specs (skill `layered-nova-regra-de-negocio`).
  Ordem: estado (409) → specs (422) → muda estado.
- Dado novo com regra de formato → value object (`record` que valida).
- Lógica entre várias entidades que não pertence a nenhuma → serviço de domínio (`domain/service`).
- Persistência nova → método no repositório concreto em `adapter/` (falando domínio). Sistema externo → skill
  `layered-nova-integracao-externa`.
- Consulta pura pode pular este passo.

## 2. Application

`application/<agregado>/<Verbo><Agregado>UC.java`.

**Ilustração** (`CancelarBoletoUC`):

```java
@ApplicationScoped
public class CancelarBoletoUC {

    private final BoletoRepository repository;            // classe concreta de adapter/database

    public CancelarBoletoUC(BoletoRepository repository) {  // injeção por construtor
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(UUID id, String motivo) {
        var boleto = repository.obter(new BoletoId(id));   // 404 automático
        boleto.cancelar(new Motivo(motivo));               // domínio decide (estado: 409)
        repository.salvar(boleto);                         // detecta concorrência
        return BoletoOutput.de(boleto);                    // nunca a entidade
    }
}
```

Com fato vindo de fora e relógio, veja `ReceberPagamentoBoletoUC`; com integração de saída depois de
salvar, `DevolverPagamentoBoletoUC`.

- Entrada com vários campos → `record *Input` em `application/<agregado>/dto`, com métodos que
  montam VOs. Saída → `*Output` com `static de(Entidade)`.
- "Hoje"/"agora" → injete `Clock`. Efeito externo → **depois** de `salvar`.
- **Proibido**: `if` de regra de negócio, `switch` em status, importar `entrypoint`, usar JPA direto.

## 3. Entrypoint(s)

- **REST**: CRUD nos verbos (`GET`, `PUT /{id}`, `DELETE /{id}`); evento de negócio/transição como
  `POST /{id}/<substantivo>` (ilustração: `/emissao`, `/cancelamento`, `/devolucao`).
- **Mensageria/IIB/fila/webhook**: entrypoint próprio (`iib.v1`, `webhook.v1`...), chamando um use case.
  Nunca duplique lógica entre entrypoints (ilustração: `PagamentoWebhookResource`).
- `*Request`: `record` com Bean Validation **só sintática** (`@NotNull`, `@NotBlank`) e `paraInput(...)`.
- `*Response`: contrato próprio com `static de(Output)`; enums viram `String`.
- **Não** crie mapper de exceção: o handler do projeto mapeia por família.

## 4. Testes

- Domínio (JUnit puro): comportamento + ordem 409/422; tabela de estados e regras, se mudaram.
- Entrypoint: caminho feliz (status + campos) e erros (status **e** `codigo`), com dados isolados por teste.

## 5. Feche

Documente o endpoint, rode a skill `layered-revisao-arquitetura` e os testes.
