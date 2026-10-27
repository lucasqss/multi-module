package com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.mapper;

import com.example.jpacore.ProcessoImobiliarioEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.jpacore.TipoImovel;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

/**
 * Domínio ↔ entidade JPA do processo imobiliário (pai + filha numa entidade só). Valores monetários voltam com
 * escala 2: o Oracle não guarda zeros à direita, e sem isso o processo lido não seria igual ao gravado.
 */
public final class ProcessoImobiliarioMapper {

    private ProcessoImobiliarioMapper() {
    }

    public static ProcessoImobiliarioEntity paraEntidade(Processo<DadosImobiliarios> p) {
        var imovel = p.dados();
        return new ProcessoImobiliarioEntity(p.id(), p.numero(), p.proponente().nome(), p.proponente().cpf().numero(),
                p.valor().quantia(), SituacaoProcesso.valueOf(p.situacao().name()), p.dataAbertura(),
                imovel.matricula(), TipoImovel.valueOf(imovel.tipoImovel().name()), imovel.valorAvaliacao(),
                imovel.uf());
    }

    public static Processo<DadosImobiliarios> paraDominio(ProcessoImobiliarioEntity e) {
        return new Processo<>(e.getId(), e.getNumero(), e.getSegmento(),
                new Proponente(e.getProponenteNome(), e.getProponenteCpf()), new Valor(e.getValor()),
                Situacao.valueOf(e.getSituacao().name()), e.getMotivoRecusa(), e.getDataAbertura(),
                new DadosImobiliarios(e.getMatricula(), DadosImobiliarios.TipoImovel.valueOf(e.getTipoImovel().name()),
                        e.getValorAvaliacao().setScale(2), e.getUf()));
    }
}
