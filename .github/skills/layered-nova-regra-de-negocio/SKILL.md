---
name: layered-nova-regra-de-negocio
description: Adiciona ou altera uma validação de negócio em projeto Java layered usando o Specification pattern (exceção específica + spec pura + registro no catálogo de regras + teste). Use quando pedirem uma nova validação, restrição, limite ou "não pode/deve" sobre os dados de uma entidade.
---

# Nova regra de negócio (Specification pattern)

> **Sobre os exemplos.** As instruções desta skill são genéricas: falam de **papéis** (catálogo de regras,
> base de estados, candidato das regras...) e valem para qualquer projeto no padrão. Os trechos marcados
> **Ilustração** vêm do repositório de referência **pagamento-boleto** (`lucasqss/multi-module`): um
> processo de pagamento de boletos, que mostra o "to be" de um sistema no padrão. Eles só ilustram: em outro projeto esses arquivos não existem, use
> os nomes do **"Mapa do projeto"** (`.github/copilot-instructions.md`) e copie a forma das classes de lá.

Regra de negócio **nunca** vai em use case, entrypoint ou adapter.

## 0. Localize os papéis

Use a seção **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, se existir. Senão, busque:

| Papel | Como achar |
|---|---|
| Base `Specification<T>` | `interface Specification<` em `domain/specification` |
| Base `RegraNegocio<T>` | `abstract class RegraNegocio` |
| Catálogo de regras do agregado | classe com constantes `ValidadorRegras<...>` (ex.: `Regras<Agregado>`) |
| Candidatos | `record`s em `domain/specification/<agregado>` usados como `T` das specs (ex.: `Solicitacao<Agregado>`) |
| Família 422 | `abstract class RegraNegocioException` |
| Regras existentes | `extends RegraNegocio<` — **leia uma antes de escrever** e copie a forma |

Se a base não existir, use antes a skill `layered-iniciar-projeto`.

## 1. Classifique a regra

| Pergunta | Se sim |
|---|---|
| É só formato do dado (tamanho, dígitos, casas decimais)? | Não é spec: valide no **value object**, exceção da família `DadoInvalidoException` (400). |
| Depende do status ("não pode X se já Y")? | Não é spec: é **State** → skill `layered-novo-estado`. |
| Sobre quais dados decide? | Use o candidato desse momento (criação, alteração, outra ação). Não existe? Crie um `record` em `domain/specification/<agregado>`, com métodos estáticos `para<Momento>(...)`. |
| Precisa de dado externo (banco, outro sistema)? | O **use case** consulta; o dado entra como campo do candidato. A spec continua pura. |

**Comando ou fato?** Se a regra avalia algo que o usuário **pede** (comando), ela recusa: `validar(...)`
lança a exceção (422). Se avalia algo que **já aconteceu** fora (fato: pagamento recebido), ela
**classifica**: `primeiraViolacao(...)` devolve a exceção sem lançar, e ela vira o motivo de um estado
de análise. A spec e a exceção são escritas do mesmo jeito nos dois casos.

## 2. Exceção específica

`domain/exception/<Situacao>Exception.java`, estendendo `RegraNegocioException`:

```java
/** <Uma frase descrevendo a situação.> */
public class LimiteDiarioExcedidoException extends RegraNegocioException {

    public static final String CODIGO = "LIMITE_DIARIO_EXCEDIDO";

    public LimiteDiarioExcedidoException(Dinheiro limite) {
        super(CODIGO, "Limite diário de " + limite + " excedido");
    }
}
```

## 3. Spec (escolha UMA forma)

**a) Classe própria** (regra com lógica própria), em `domain/specification/<agregado>/`.
Ilustração HIPOTÉTICA no processo de pagamento (um limite por beneficiário que não existe lá):

```java
/** <Regra em uma frase.> */
public class LimitePorBeneficiarioSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return s.boletos().boletos().stream()              // só lê o candidato
                .noneMatch(b -> b.valor().maiorQue(LIMITE));
    }

    @Override
    protected LimitePorBeneficiarioExcedidoException violacao(SubmissaoProcesso s) {
        return new LimitePorBeneficiarioExcedidoException(LIMITE);
    }
}
```

**b) Composição no catálogo** (regra condicional/combinação) a partir de blocos `Specification<T>`.
**Ilustração** (real, em `RegrasProcessoPagamento`). Composição também serve para DECISÃO sem erro:
a alçada é uma `Specification` pura que o estado usa para escolher o destino.

```java
public static final RegraNegocio<SubmissaoProcesso> VALOR_TOTAL_DENTRO_DO_LIMITE = RegraNegocio.de(
        new ValorTotalAteSpec(LIMITE_POR_PROCESSO),
        s -> new ValorTotalAcimaDoLimiteException(LIMITE_POR_PROCESSO));

/** Alçada: acima de R$ 10.000 exige duas aprovações. É DECISÃO, não erro. */
public static final Specification<AprovacaoProcesso> ALCADA_ATINGIDA =
        quando(new ValorAcimaDeSpec(LIMITE_UMA_APROVACAO)).entao(new AprovacoesNoMinimoSpec(2));
```

A spec é **pura**: nada de I/O, `Clock`, `now()` ou injeção.

## 4. Registre no catálogo

Adicione ao conjunto do **momento** certo (ilustração: `RegrasProcessoPagamento` tem `BOLETOS`,
`SUBMISSAO`, `APROVACAO`, `ALCADA_ATINGIDA` e `CONFORMIDADE_RETORNO`): `<SECAO>` (ao salvar uma seção; o agregado pode estar
incompleto), `FINALIZACAO` (completude, ao finalizar a edição), `<ACAO>` (outras ações). **A posição importa**:
o validador é fail-fast e o cliente recebe a primeira regra violada. Fundamentais antes das de detalhe.

## 5. Se precisar de um fato novo

1. Campo novo no candidato + ajuste nos métodos estáticos `para<Momento>(...)` que o montam.
2. Consulta nova no repositório/integração de `adapter/` (devolvendo tipo de domínio ou primitivo).
3. No use case: consultar e passar para `Solicitacao<Agregado>.para<Momento>(...)`.
4. Atualizar o builder de cenários dos testes.

## 6. Testes

- Teste de regras: parta do cenário válido, quebre **só** o necessário, verifique o **tipo** da
  exceção (`assertThrows(LimiteDiarioExcedidoException.class, ...)`) e o limite que passa.
- Se muda a API: teste HTTP com `422` e o `codigo`.

## 7. Feche

Atualize a documentação de regras do projeto (README), rode a skill `layered-revisao-arquitetura`
(script `verificar-camadas.sh`) e os testes. **Não** mexa no handler HTTP nem em use cases que não
precisam do fato novo.
