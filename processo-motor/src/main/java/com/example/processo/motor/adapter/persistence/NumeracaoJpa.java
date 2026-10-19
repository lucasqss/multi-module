package com.example.processo.motor.adapter.persistence;

import com.example.processo.motor.domain.Numeracao;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

@Repository
public interface NumeracaoJpa extends Numeracao {

    /** Sequence do Oracle: único para todos os segmentos e sem corrida entre transações. */
    @Override
    @SQL("select SEQ_NUMERO_PROCESSO.nextval from dual")
    long proximo();
}
