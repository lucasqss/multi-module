# 08 — Arquitetura da solução unificada

Diagrama: [`diagramas/puml/08-componentes-solucao-unificada.puml`](diagramas/puml/08-componentes-solucao-unificada.puml)

## Princípio

**Um único fluxo** (o BPMN principal) e **pontos de variação explícitos**. Tudo o que muda entre modalidades, regimes ou administradoras é resolvido por uma das três fontes:

| Fonte de variação | Mecanismo | Exemplo |
|---|---|---|
| Modalidade / finalidade | `PoliticaModalidade` (*Strategy*) | Garantia padrão do serviço ≠ do imóvel |
| Regime normativo | Resolvedor de Regras (por `grupo.regimeNormativo`) | D+3 da homologação × da contemplação |
| Regulamento / administradora | `RegulamentoGrupo` + configuração de práticas | Prazo de análise, retenção final, idade máxima de usados |

## Componentes

| Componente | Responsabilidade |
|---|---|
| Orquestrador de Utilização | Executa o BPMN; mantém o estado da `SolicitacaoUtilizacao`; emite eventos |
| Resolvedor de Regras | Devolve as regras aplicáveis por (regime, segmento, subcategoria, finalidade) e os parâmetros do regulamento. Candidato a tabelas DMN |
| Calendário de dias úteis | Feriados nacionais + estaduais/municipais **da localidade de constituição do grupo** (art. 52) |
| PoliticaModalidade | `validarElegibilidade`, `documentosExigidos`, `garantiaPadrao`, `planoPagamentoPadrao`, `despesasVinculadasAdmitidas` |
| Gestão de Garantias | Ciclo de vida da garantia, integração com SNG/RI/RTD, substituição, baixa |
| Gestão Documental | Checklist por finalidade, validade, versionamento, retenção |
| Conta do Crédito | Saldo utilizável, rendimentos, atualização, movimentos (fonte para a contabilidade do grupo) |
| Motor de Pagamentos | À vista = plano de 1 etapa; etapas; sobra; espécie; ressarcimento de sinal. Garante RN-06 e RN-18 |
| Trilha de Auditoria | Evidências exigidas (capacidade, comunicação formal, contratos de adiantamento, comunicações com controle de recebimento) |

## Contrato da política de modalidade (pseudocódigo)

```text
interface PoliticaModalidade {
  suporta(segmento, subcategoria, finalidade): boolean
  validarElegibilidade(objeto, grupo): Resultado            // RN-09, RN-10, RN-23
  documentosExigidos(finalidade, regulamento): TipoDocumento[] // RN-17
  garantiaPadrao(objeto): EspecificacaoGarantia            // RN-11
  planoPagamentoPadrao(objeto): PlanoPagamento             // À vista | Etapas
  despesasVinculadasAdmitidas(regime): TipoDespesa[]       // RN-16
}
```

Implementações de referência: `PoliticaBemMovel`, `PoliticaImovel` (variantes pronto, planta, terreno, construção, reforma), `PoliticaServico` e `PoliticaQuitacaoFinanciamento`.

## Eventos de domínio sugeridos

`ContemplacaoHomologada`, `CreditoDisponibilizado`, `FinalidadeIndicada`, `AnaliseConcluida`, `GarantiaRegistrada`, `PagamentoEfetivado`, `EtapaLiberada`, `SobraDestinada`, `CreditoUtilizado`, `PrazoEspecieAtingido`, `AtrasoPosUtilizacao`, `CotaQuitada`, `GarantiaBaixada`, `UltimaAGOContemplacao`.

## Decisões em aberto (para ADR)

1. **Engine de workflow** (Camunda/Zeebe, Temporal, máquina de estados própria).
2. **Regras em DMN × código**: DMN facilita auditoria do rótulo `[NORMA]/[CONTRATO]/[PRÁTICA]`.
3. **Fonte do saldo**: conta do crédito própria × consulta ao sistema de grupos.
4. **Consórcio já utilizado como financiamento quitável** (art. 15 §1º): exige integração entre cotas da mesma administradora ou de terceiros.
