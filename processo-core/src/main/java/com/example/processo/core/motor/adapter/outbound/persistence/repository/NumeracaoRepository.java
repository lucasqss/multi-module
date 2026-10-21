package com.example.processo.core.motor.adapter.outbound.persistence.repository;

import com.example.processo.core.motor.application.port.out.Numeracao;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

/** Implementa o port direto: a implementação é gerada pelo Hibernate Processor. */
@Repository
public interface NumeracaoRepository extends Numeracao {

    /** Sequence do Oracle: única para todos os segmentos e sem corrida entre transações. */
    @Override
    @SQL("select SEQ_NUMERO_PROCESSO.nextval from dual")
    long proximo();
}
