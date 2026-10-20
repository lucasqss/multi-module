package com.example.processo.core.motor.adapter.outbound.persistence.repository;

import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

/** Jakarta Data puro: a implementação é gerada pelo Hibernate Processor. */
@Repository
public interface NumeracaoRepository {

    /** Sequence do Oracle: única para todos os segmentos e sem corrida entre transações. */
    @SQL("select SEQ_NUMERO_PROCESSO.nextval from dual")
    long proximo();
}
