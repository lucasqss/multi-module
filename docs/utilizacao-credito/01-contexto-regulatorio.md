# 01 — Contexto regulatório e regime normativo

## 1. Normas que regem a utilização do crédito

| Norma | O que importa para a utilização |
|---|---|
| **Lei 11.795/2008** | Objeto (art. 12), cessão com anuência (art. 13), **garantias (art. 14)**, quitação de financiamento (art. 22 §3º), valor do crédito e rendimentos (art. 24), encerramento e recursos não procurados (arts. 31-36), ato único no registro imobiliário (art. 45) |
| **Resolução BCB 285/2023** (vigente desde 01/07/2024, alterada pela Res. BCB 362/2023) | Cláusulas obrigatórias do contrato (art. 2º), capacidade de pagamento (art. 3º), subcategorias de móveis (art. 5º §2º), lance e homologação (arts. 12-13), **finalidades (art. 15)**, D+3 (art. 16), rendimentos (art. 17), **pagamento ao fornecedor e sobra (art. 18)**, execução (arts. 27-28), exclusão e crédito parcial (arts. 32-33), dias úteis (art. 52), transição (art. 57) |
| **Circular BCB 3.432/2009** | Rege a utilização de crédito dos **grupos constituídos antes de 01/07/2024** |
| **DL 911/1969 + Lei 4.728/1965 art. 66-B** | Alienação fiduciária de bens móveis; busca e apreensão (extrajudicial, arts. 8º-B a 8º-E, se o contrato prever — Lei 14.711/2023) |
| **Lei 9.514/1997** | Alienação fiduciária de imóveis, consolidação e leilões (alterada pela Lei 14.711/2023) |
| **Lei 8.036/1990 + normas do Conselho Curador/CAIXA** | Uso do FGTS em consórcio imobiliário (Res. 285 art. 53) |
| **Circular BCB 3.978/2020** | PLD/FT: pagamentos a terceiros, espécie, cessões, composição de recursos |

## 2. Regra mestra: regime normativo do grupo

| Data da 1ª AGO do grupo | Regime para a utilização do crédito |
|---|---|
| **≥ 01/07/2024** | `RES_285`: Res. BCB 285 integral |
| **< 01/07/2024** | `CIRC_3432`: Circular 3.432 até o encerramento, **exceto** os arts. 14, 25-A, 25-B, 36, 44, 49, 51, 53, 54 e 55 da Res. 285 (art. 57) |

### Diferenças que mudam o comportamento do sistema

| Tema | `CIRC_3432` | `RES_285` |
|---|---|---|
| Marco do D+3 | 3º dia útil após a **contemplação** (art. 11) | 3º dia útil após a **homologação** (art. 16) |
| Homologação do lance | sem regra expressa | **só após o recebimento** do lance (art. 12 II) |
| Imóvel — localização | município onde a administradora opera, salvo autorização (art. 5º XIII-a-3) | sem restrição na norma |
| Sinal pago após a contemplação | ressarcível em espécie “até o montante” (art. 12 §1º) | ressarcível, **deduzido do crédito** (art. 18 §1º) |
| Sobra — 10% para despesas | transferência, tributos, cartório, registro, seguros | os mesmos itens **+ tarifas e ressarcimento à administradora** |
| Contemplado inadimplente que ainda não usou o crédito | AGO pode **cancelar a contemplação** (art. 10) | **exclusão** após 3 vencimentos (art. 32 II) → **crédito parcial** (art. 33) |
| Exclusão de contemplado | vedada para **qualquer** contemplado | vedada só para quem **já usou** o crédito na aquisição (art. 32 p.u.) |

> **Implicação de design:** `regimeNormativo` é atributo do `Grupo`, calculado da data da 1ª AGO e imutável. Cada regra do catálogo ([07](07-regras-de-negocio.md)) declara a quais regimes se aplica.

## 3. O que a norma delega ao contrato (Res. 285 art. 2º)

Estes itens **têm de vir do regulamento do grupo** e não podem ser fixos no código:

- XIV-a: prazo de pagamento do lance;
- XIV-b: procedimentos e **prazos de análise** da documentação para liberação;
- XIV-c/d: disponibilização após homologação e procedimento de pagamento ao fornecedor;
- XIV-e: transferência de cota com análise de capacidade do novo titular;
- **XV: garantias exigidas e procedimento de substituição**;
- XVI: autorização e dados de conta/Pix para transferências ao consorciado;
- XVIII-a: exigências para aquisição pós-contemplação;
- **XVIII-c: índice de atualização do crédito após a contemplação**;
- XVIII-d: procedimento de pagamento ao fornecedor.

## 4. Finalidades admitidas do crédito (Res. 285 art. 15)

1. **Aquisição** em fornecedor escolhido pelo consorciado:
   a) móvel **subcategoria I** (veículos, aeronaves, embarcações, máquinas e equipamentos), **novo ou usado**;
   b) móvel **subcategoria II**, **somente novo**;
   c) imóvel construído ou na planta, inclusive terreno, com **construção ou reforma** nos termos do contrato;
   d) imóvel em empreendimento imobiliário, se o contrato assim referenciar;
   e) serviço ou conjunto de serviços.
2. **Quitação total de financiamento** de titularidade do consorciado e da mesma categoria. Inclui arrendamento mercantil financeiro e **consórcio já utilizado** (§1º).
3. **Crédito parcial** ao excluído contemplado.
4. Pagamentos à administradora nas hipóteses da Resolução.
5. Sobra (art. 18 §3º) e fundo de reserva (art. 22).
6. **Espécie**, com ≥ 180 dias da contemplação **e** quitação (§2º).

Fora dessas hipóteses o uso é vedado.
