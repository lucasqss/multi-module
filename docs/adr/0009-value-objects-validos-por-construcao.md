# 0009. Value objects válidos por construção

- **Status:** Aceita
- **Data:** 2026-10-27

## Contexto

CPF e valor circulavam como `String` e `BigDecimal`, validados por uma regra. Nada impedia um CPF inválido de
chegar a um port, e o Oracle devolve `400000` para `400000.00`, o que quebrava a igualdade entre o processo gravado
e o lido.

## Decisão

`Cpf` e `Valor` são records que validam no construtor ("parse, don't validate"): CPF com dígitos verificadores;
valor maior que zero, com no máximo 2 casas e escala sempre 2. São construídos onde o texto entra no domínio (os
DTOs do REST, os mappers de persistência); dali para dentro, ninguém valida de novo.

## Consequências

- Tipos mais expressivos nas assinaturas (`SituacaoCadastral.regular(Cpf)`).
- Entrada inválida é recusada antes do motor (422 com a mesma mensagem). Com nome vazio e CPF inválido juntos, o
  erro do CPF vem primeiro.
- Candidatos naturais ao mesmo tratamento: placa, matrícula, UF.
