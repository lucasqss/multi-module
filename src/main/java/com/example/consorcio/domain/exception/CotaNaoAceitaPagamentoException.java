package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.valueobject.IdentificadorCota;

/** Cota cancelada/encerrada não recebe pagamento. */
public class CotaNaoAceitaPagamentoException extends RegraNegocioException {

    public static final String CODIGO = "COTA_NAO_ACEITA_PAGAMENTO";

    public CotaNaoAceitaPagamentoException(IdentificadorCota cota, SituacaoCota situacao) {
        super(CODIGO, "Cota %s está %s e não aceita pagamentos".formatted(cota, situacao));
    }
}
