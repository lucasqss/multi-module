package com.example.pagamento.domain.valueobject;

/**
 * Como o pagamento agrupa os boletos. Fixa na criação. As regras que variam por modalidade ficam
 * nos elos da {@code CadeiaValidacao} de {@code RegrasPagamento}, não em {@code if}s no agregado.
 */
public enum Modalidade {
    /** Um único boleto. */
    INDIVIDUAL,
    /** Vários boletos de UM fornecedor (ex.: faturas do mês). */
    PROCESSO,
    /** Vários boletos de fornecedores diferentes, pagos juntos. */
    LOTE
}
