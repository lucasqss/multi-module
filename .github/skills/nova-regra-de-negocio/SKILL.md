---
name: nova-regra-de-negocio
description: Adiciona ou altera uma validação de negócio usando o Specification pattern (exceção específica + spec + registro no catálogo RegrasBoleto + teste). Use sempre que pedirem uma nova validação, restrição, limite ou "não pode/deve" sobre os dados do boleto ou do pagamento.
---

# Nova regra de negócio (Specification pattern)

Regra de negócio **nunca** vai em use case, resource ou adapter. Siga os passos na ordem.

## 1. Classifique a regra

| Pergunta | Se sim |
|---|---|
| É só formato do dado (tamanho, dígitos, casas decimais)? | Não é spec: valide no **value object** com exceção da família `DadoInvalidoException` (400). |
| Depende do status do boleto ("não pode X se já pago")? | Não é spec: é **State** → skill `novo-estado`. |
| Sobre que dados ela decide? | Emissão/alteração → candidato `SolicitacaoBoleto`. Recebimento → `RecebimentoPagamento`. Outro momento → crie um candidato novo (`record`) em `domain/specification/boleto`. |
| Precisa de dado externo (banco, outro sistema, feriados)? | O **use case** consulta e o dado entra como campo do candidato. A spec continua pura. |

## 2. Crie a exceção específica

`src/main/java/com/example/boleto/domain/exception/<Situacao>Exception.java`, estendendo
`RegraNegocioException` (422). Modelo: `BoletoVencidoException`.

```java
/** <Uma frase descrevendo a situação.> */
public class ValorAcimaDoLimiteDiarioException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_ACIMA_DO_LIMITE_DIARIO";

    public ValorAcimaDoLimiteDiarioException(Dinheiro limite) {
        super(CODIGO, "Soma dos boletos do dia excede " + limite);
    }
}
```

## 3. Crie a spec (escolha UMA forma)

**a) Regra simples → classe própria** em `domain/specification/boleto/`.
Modelo: `VencimentoEmDiaUtilSpec`, `LimiteBoletosEmAbertoSpec`.

```java
// Exemplo HIPOTÉTICO: usa um fato novo (totalEmitidoHojeDoPagador, ver passo 5)
// e um método somar(...) que ainda não existe em Dinheiro.
/** <Regra em uma frase.> */
public class LimiteDiarioSpec extends RegraNegocio<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return !s.totalEmitidoHojeDoPagador().somar(s.dados().valor()).maiorQue(LIMITE);
    }

    @Override
    protected ValorAcimaDoLimiteDiarioException violacao(SolicitacaoBoleto s) {
        return new ValorAcimaDoLimiteDiarioException(LIMITE);
    }
}
```

**b) Regra condicional/combinação → compor no catálogo** com blocos `Specification<T>`
(`ValorAteSpec`, `ValorMinimoSpec`, `PagadorPessoaFisicaSpec`). Crie blocos novos se preciso.

```java
public static final RegraNegocio<SolicitacaoBoleto> LIMITE_PESSOA_FISICA_RESPEITADO = RegraNegocio.de(
        quando(new PagadorPessoaFisicaSpec()).entao(new ValorAteSpec(LIMITE_PESSOA_FISICA)),
        s -> new LimitePessoaFisicaExcedidoException(LIMITE_PESSOA_FISICA));
```

Regras da spec: **pura** (sem I/O, `Clock`, injeção, `now()`); só lê o candidato.

## 4. Registre no catálogo

Em `domain/specification/boleto/RegrasBoleto.java`, adicione ao conjunto certo:
`COMUNS` (emissão e alteração), `EMISSAO` (só emissão), `ALTERACAO`, `RECEBIMENTO`.
**A posição importa**: é fail-fast, a primeira regra violada é a que o cliente recebe. Regras mais
fundamentais (limites de valor) vêm antes das de detalhe.

## 5. Se a regra precisa de um fato novo

1. Adicione o campo ao candidato (`SolicitacaoBoleto`) e aos métodos `paraEmissao`/`paraAlteracao`.
2. Adicione a consulta em `adapter/database/BoletoRepository` (devolvendo tipo de domínio ou primitivo).
3. No use case, consulte e passe para `SolicitacaoBoleto.para<Momento>(...)` (modelo: `contarEmAbertoPorPagador` em `EmitirBoletoUC`).
4. Atualize o builder de teste `Cenarios`.

## 6. Testes

- Em `RegrasBoletoTest` (classe `@Nested` do conjunto): parta de `solicitacao()` válido, quebre **só**
  o necessário e verifique o **tipo**: `assertViola(MinhaException.class, solicitacao().valor(...))`.
  Teste também o limite que passa (`assertValido`).
- Se o comportamento muda na API, um caso em `BoletoResourceTest` verificando `422` e `codigo`.

## 7. Feche

- Atualize a tabela "Regras fake implementadas" do `README.md`.
- Rode `.github/skills/revisao-arquitetura/verificar-camadas.sh` e `mvn test`.
- **Não** altere `ApiExceptionHandler` nem use cases que não precisam do fato novo.
