package com.example.processo.core.motor.application.port.spi;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;

import java.util.Optional;
import java.util.UUID;

/**
 * O que todo segmento faz igual: identificação (código, prefixo, tipo dos dados) e gravar/ler pela persistência dele.
 * Um segmento novo estende esta classe e escreve só o que é dele: {@link #regras()} e {@link #avaliar}.
 *
 * <p>Os segmentos são {@code @Singleton}: não guardam estado e não precisam de proxy. Um bean {@code @ApplicationScoped}
 * exigiria um construtor sem argumentos também nesta base.
 */
public abstract class SegmentoBase<D extends DadosDoSegmento> implements Segmento<D> {

    private final String codigo;
    private final String prefixo;
    private final Class<D> tipoDosDados;
    private final PersistenciaDoSegmento<D> persistencia;

    protected SegmentoBase(String codigo, String prefixo, Class<D> tipoDosDados, PersistenciaDoSegmento<D> persistencia) {
        this.codigo = codigo;
        this.prefixo = prefixo;
        this.tipoDosDados = tipoDosDados;
        this.persistencia = persistencia;
    }

    @Override
    public String codigo() {
        return codigo;
    }

    @Override
    public String prefixo() {
        return prefixo;
    }

    @Override
    public Class<D> tipoDosDados() {
        return tipoDosDados;
    }

    @Override
    public void salvar(Processo<D> processo) {
        persistencia.salvar(processo);
    }

    @Override
    public Optional<Processo<D>> carregar(UUID id) {
        return persistencia.porId(id);
    }
}
