---
applyTo: "src/test/java/**/*.java"
---

# Testes

- **Domínio = JUnit puro**, sem `@QuarkusTest`. Use o builder `Cenarios` (cenário válido por padrão;
  cada teste muda só o que interessa) e datas fixas (`Cenarios.HOJE` é uma terça-feira).
- Regras: um teste por regra em `RegrasBoletoTest`, verificando o **tipo** da exceção
  (`assertThrows(BoletoVencidoException.class, ...)`), nunca a mensagem.
- Estados: mantenha a **tabela** `PERMITIDAS` de `EstadoBoletoTest` em dia com qualquer transição.
- Entidade: teste que ela **aplica** estado e regras (e a ordem 409 antes de 422), não as regras em si.
- `@QuarkusTest` só para API (`BoletoResourceTest`) e adapters (`ConcorrenciaTest`). Em testes de API,
  use um documento de pagador diferente por teste para não haver interferência entre eles.
- Asserções HTTP verificam status **e** `codigo` do corpo de erro.
