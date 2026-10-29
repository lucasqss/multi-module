# 04 — Modelo de domínio

Diagramas:
- [`diagramas/puml/02-modelo-dominio.puml`](diagramas/puml/02-modelo-dominio.puml): classes e associações
- [`diagramas/puml/03-maquina-estados-credito.puml`](diagramas/puml/03-maquina-estados-credito.puml): ciclo de vida do crédito

## Agregados

| Agregado | Raiz | Conteúdo | Por que é uma fronteira |
|---|---|---|---|
| Grupo | `Grupo` | `RegulamentoGrupo`, regime, segmento, subcategoria, localidade | As regras resolvem-se por grupo; é imutável no que importa ao regime |
| Crédito | `CreditoContemplado` | `MovimentoCredito`, `SolicitacaoUtilizacao`, objetos, documentos, plano, sobra | O saldo utilizável precisa de consistência transacional entre pagamentos e destinações |
| Garantia | `Garantia` | `RegistroGarantia` | Tem ciclo de vida próprio, que vai além da utilização (até a quitação da cota) |
| Beneficiário | `Beneficiario` | conta, `ContratoAdiantamento` | É reutilizado entre cotas e sujeito a KYC próprio |

## Conceitos centrais

- **CreditoContemplado**: o “dinheiro da carta”. `saldoUtilizavel = valorAGO − lanceEmbutido + rendimentos + atualização − pagamentos − destinações`. Cada alteração do saldo é um `MovimentoCredito`, o que facilita a conciliação com a contabilidade do grupo.
- **SolicitacaoUtilizacao**: uma intenção de uso com finalidade única. Um crédito pode ter **várias** solicitações sequenciais (ex.: terreno e depois construção).
- **ObjetoUtilizacao**: hierarquia `BemMovel | BemImovel | Servico | FinanciamentoQuitavel`. `emProducao = true` aciona a garantia alternativa (art. 14 §3º).
- **ComposicaoRecursos**: de onde vem cada real que paga o objeto (carta, próprios, FGTS, sobra). Serve ao PLD e ao cálculo da garantia sobre o bem inteiro.
- **PlanoPagamento / Etapa / OrdemPagamento**: a forma de pagar. À vista = plano com uma etapa; isso unifica o motor de pagamentos.
- **Garantia**: `provisoria` e a autoassociação `substitui` cobrem planta/obra e quitação de financiamento.
- **AnaliseCapacidade**: evidência obrigatória com data de retenção calculada (`encerramentoGrupo + 5 anos`).

## Estados do crédito (resumo)

```
PendenteHomologacao → Homologada → Disponibilizado → EmUtilizacao{AguardandoEscolha → EmAnalise ⇄ Pendente
  → (JanelaRisco) → EmGarantia → Apto → (Etapas)} → Utilizado{Sobra → GarantiaAtiva}
  → GarantiaBaixada | EmExecucaoGarantia
Desvios: ElegivelEspecie → PagoEmEspecie · ExcluidoComCreditoParcial (RES_285) · ContemplacaoCancelada (CIRC_3432) · NaoProcurado
```

## Invariantes do domínio

1. `OrdemPagamento.beneficiario` ≠ consorciado, exceto nas finalidades `ESPECIE`, `CREDITO_PARCIAL_EXCLUIDO`, ressarcimento de sinal e devolução de sobra (RN-06).
2. Σ `DestinacaoSobra[DESPESAS_VINCULADAS]` ≤ 10% do crédito da contemplação (RN-16).
3. `Garantia.situacao = BAIXADA` só se `Cota.saldoDevedor = 0` (RN-12).
4. `Grupo.subcategoria = II` ⇒ `BemMovel.novo = true` (RN-09).
5. `FinanciamentoQuitavel.titularCpfCnpj = Consorciado.cpfCnpj` e `saldoQuitacao` totalmente coberto (RN-10).
6. Crédito em `Utilizado` ⇒ a cota não pode transitar para exclusão (RN-14).
