# 0012. Cadastro simples pelo caminho curto, sem motor

- **Status:** Aceita (o cadastro está em `negocio/agencia` desde a [0014](0014-camadas-num-modulo-so.md))
- **Data:** 2026-10-27

## Contexto

O modelo do processo (motor, SPI de segmento, padrão State, ports com fakes, lado de leitura separado) existe porque
processos têm fluxo, estados e variação por tipo. Uma entidade que é só cadastro (incluir, alterar, excluir,
consultar) não tem nada disso; copiar o modelo do processo para ela seria cerimônia, e a próxima pessoa copiaria a
cerimônia também.

## Decisão

Cadastro simples vai pelo caminho curto, num pacote plano `processo-core/.../cadastro/<nome>`:

- record válido por construção (inválido → `RegraVioladaException` → 422);
- repository Jakarta Data usado direto, sem port;
- um serviço `@ApplicationScoped` com `@Transactional` nas escritas, que converte record ↔ entidade e é a entrada
  do core para o REST;
- leitura pelo mesmo serviço, sem `processo-query`;
- teste pela API, num IT do ciclo completo.

O exemplo é o cadastro de agências (`cadastro.agencia`, `/agencias`).

## Consequências

- Um CRUD custa 4 arquivos no core, 3 no app, a entidade e a migration.
- Sem fakes, a regra do cadastro é testada só pelo IT (com Oracle). Aceitável enquanto a regra for validação de
  campos.
- **Quando promover para o modelo do processo:** quando surgir fluxo (aprovar, processar), estados, regra que consulta
  sistema externo ou variação por tipo. A promoção é local: o record, o repository e o REST continuam.
