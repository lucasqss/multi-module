---
applyTo: "**/src/test/java/**/*.java"
---

# Testes

- **Domínio = JUnit puro**, sem subir o framework. Use um builder de cenário válido por padrão
  (ex.: `Cenarios`), em que cada teste muda só o que interessa, e datas fixas (nunca `now()`).
- Regras: um teste por regra no teste do catálogo (`Regras<Agregado>Test`), verificando o **tipo**
  da exceção (`assertThrows(<Situacao>Exception.class, ...)`), nunca a mensagem.
- Estados: mantenha a **tabela** de transições (`PERMITIDAS`) do teste de estados em dia.
- Entidade: teste que ela **aplica** estado e regras (e a ordem 409 antes de 422), não as regras em si.
- Teste de integração (`@QuarkusTest`/`@SpringBootTest`) só para entrypoints e adapters.
  Isole os dados de cada teste (ids/documentos próprios) para não haver interferência.
- Asserções HTTP verificam status **e** `codigo` do corpo de erro.
