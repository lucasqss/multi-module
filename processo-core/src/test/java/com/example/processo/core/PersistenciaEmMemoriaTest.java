package com.example.processo.core;

import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;

/** Os fakes usados nos testes do motor e dos segmentos cumprem o mesmo contrato que os repositories reais. */
class PersistenciaEmMemoriaTest extends PersistenciaDoProcessoContrato {

    final Fakes.BancoEmMemoria banco = new Fakes.BancoEmMemoria();
    final Fakes.ProcessosImobiliariosEmMemoria imobiliarios = new Fakes.ProcessosImobiliariosEmMemoria(banco);

    @Override
    protected Processos processos() {
        return banco;
    }

    @Override
    protected ProcessosImobiliarios imobiliarios() {
        return imobiliarios;
    }

    @Override
    protected Transacoes transacoes() {
        return Fakes.semTransacao();
    }
}
