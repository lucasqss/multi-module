package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.model.Cota;

public class CotaAceitaPagamentoSpec implements Specification<Cota> {

    @Override
    public boolean isSatisfiedBy(Cota cota) {
        return cota.aceitaPagamento();
    }

    @Override
    public String codigo() {
        return "COTA_NAO_ACEITA_PAGAMENTO";
    }

    @Override
    public String mensagem(Cota cota) {
        return "Cota %s está %s e não aceita pagamentos".formatted(cota.identificador(), cota.situacao());
    }
}
