package com.example.processo;

import com.example.processo.core.PersistenciaDoProcessoContrato;
import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * O contrato dos ports de persistência contra os repositories Jakarta Data no Oracle (Dev Services). É o mesmo
 * contrato que os fakes cumprem no processo-core: se o banco e o fake divergirem, um dos dois quebra.
 */
@QuarkusTest
// Os mesmos recursos do NovosProcessosPorFilaTest: com o mesmo conjunto, os dois testes compartilham uma subida do
// app (e do Oracle). Um conjunto diferente faria o Quarkus reiniciar.
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class PersistenciaOracleTest extends PersistenciaDoProcessoContrato {

    @Inject
    Processos processos;

    @Inject
    ProcessosImobiliarios imobiliarios;

    @Inject
    Transacoes transacoes;

    @Override
    protected Processos processos() {
        return processos;
    }

    @Override
    protected ProcessosImobiliarios imobiliarios() {
        return imobiliarios;
    }

    @Override
    protected Transacoes transacoes() {
        return transacoes;
    }
}
