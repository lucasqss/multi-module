package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.IdentificadorCota;

/** Só pode haver um processo em andamento por cota. */
public class CotaComProcessoEmAndamentoException extends RegraNegocioException {

    public static final String CODIGO = "COTA_COM_PROCESSO_EM_ANDAMENTO";

    public CotaComProcessoEmAndamentoException(IdentificadorCota cota) {
        super(CODIGO, "Cota %s já possui um processo de pagamento em andamento".formatted(cota));
    }
}
