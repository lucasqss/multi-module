package com.example.processo.core.segmento.veicular.adapter.outbound.persistence.mapper;

import com.example.jpacore.ProcessoVeicularEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

/**
 * Domínio ↔ entidade JPA do processo veicular (pai + filha numa entidade só). Valores monetários voltam com escala
 * 2: o Oracle não guarda zeros à direita.
 */
public final class ProcessoVeicularMapper {

    private ProcessoVeicularMapper() {
    }

    public static ProcessoVeicularEntity paraEntidade(Processo<DadosVeiculares> p) {
        var veiculo = p.dados();
        return new ProcessoVeicularEntity(p.id(), p.numero(), p.proponente().nome(), p.proponente().cpf().numero(),
                p.valor().quantia(), SituacaoProcesso.valueOf(p.situacao().name()), p.dataAbertura(),
                veiculo.placa(), veiculo.anoFabricacao(), veiculo.valorFipe());
    }

    public static Processo<DadosVeiculares> paraDominio(ProcessoVeicularEntity e) {
        return new Processo<>(e.getId(), e.getNumero(), e.getSegmento(),
                new Proponente(e.getProponenteNome(), e.getProponenteCpf()), new Valor(e.getValor()),
                Situacao.valueOf(e.getSituacao().name()), e.getMotivoRecusa(), e.getDataAbertura(),
                new DadosVeiculares(e.getPlaca(), e.getAnoFabricacao(), e.getValorFipe().setScale(2)));
    }
}
