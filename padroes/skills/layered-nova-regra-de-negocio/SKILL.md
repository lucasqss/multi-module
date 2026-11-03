---
name: layered-nova-regra-de-negocio
description: Adiciona ou altera uma validação de negócio em projeto Java layered usando o Specification pattern (exceção específica + spec pura + registro no catálogo de regras + teste). Use quando pedirem uma nova validação, restrição, limite ou "não pode/deve" sobre os dados de uma entidade.
---

# Nova regra de negócio (Specification pattern)

Regra de negócio **nunca** vai em use case, entrypoint ou adapter.

## 0. Localize os papéis

Use a seção **"Mapa do projeto"** do `copilot-instructions.md`/`AGENTS.md`, se existir. Senão, busque:

| Papel | Como achar |
|---|---|
| Base `Specification<T>` | `interface Specification<` em `domain/specification` |
| Base `RegraNegocio<T>` | `abstract class RegraNegocio` |
| Catálogo de regras do agregado | classe com constantes `ValidadorRegras<...>` (ex.: `Regras<Agregado>`) |
| Candidatos | `record`s em `domain/model` usados como `T` das specs |
| Família 422 | `abstract class RegraNegocioException` |
| Regras existentes | `extends RegraNegocio<` — **leia uma antes de escrever** e copie a forma |

Se a base não existir, use antes a skill `layered-iniciar-projeto`.

## 1. Classifique a regra

| Pergunta | Se sim |
|---|---|
| É só formato do dado (tamanho, dígitos, casas decimais)? | Não é spec: valide no **value object**, exceção da família `DadoInvalidoException` (400). |
| Depende do status ("não pode X se já Y")? | Não é spec: é **State** → skill `layered-novo-estado`. |
| Sobre quais dados decide? | Use o candidato desse momento (criação, alteração, outra ação). Não existe? Crie um `record` em `domain/model`. |
| Precisa de dado externo (banco, outro sistema)? | O **use case** consulta; o dado entra como campo do candidato. A spec continua pura. |

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
Exemplo com o agregado **Contrato** e seu candidato `SolicitacaoContrato`:

```java
/** <Regra em uma frase.> */
public class LimiteDiarioSpec extends RegraNegocio<SolicitacaoContrato> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoContrato s) {
        return !s.totalDoDia().maiorQue(LIMITE);      // só lê o candidato
    }

    @Override
    protected LimiteDiarioExcedidoException violacao(SolicitacaoContrato s) {
        return new LimiteDiarioExcedidoException(LIMITE);
    }
}
```

**b) Composição no catálogo** (regra condicional/combinação) a partir de blocos `Specification<T>`:

```java
public static final RegraNegocio<SolicitacaoContrato> LIMITE_PF_RESPEITADO = RegraNegocio.de(
        quando(new PessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PF)),
        s -> new LimitePessoaFisicaExcedidoException(LIMITE_PF));
```

A spec é **pura**: nada de I/O, `Clock`, `now()` ou injeção.

## 4. Registre no catálogo

Adicione ao conjunto certo (ex.: `COMUNS`, `CRIACAO`, `ALTERACAO`, `<ACAO>`). **A posição importa**:
o validador é fail-fast e o cliente recebe a primeira regra violada. Fundamentais antes das de detalhe.

## 5. Se precisar de um fato novo

1. Campo novo no candidato + ajuste na factory que monta o candidato.
2. Consulta nova na porta (`domain/repository` ou gateway) + implementação no adapter.
3. No use case: consultar e passar para a factory.
4. Atualizar o builder de cenários dos testes.

## 6. Testes

- Teste de regras: parta do cenário válido, quebre **só** o necessário, verifique o **tipo** da
  exceção (`assertThrows(LimiteDiarioExcedidoException.class, ...)`) e o limite que passa.
- Se muda a API: teste HTTP com `422` e o `codigo`.

## 7. Feche

Atualize a documentação de regras do projeto (README), rode a skill `layered-revisao-arquitetura`
(script `verificar-camadas.sh`) e os testes. **Não** mexa no handler HTTP nem em use cases que não
precisam do fato novo.
