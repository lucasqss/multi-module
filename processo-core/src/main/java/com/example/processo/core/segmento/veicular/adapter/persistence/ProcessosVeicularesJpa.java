package com.example.processo.core.segmento.veicular.adapter.persistence;

import com.example.jpacore.ProcessoVeicularEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.estado.Situacao;
import com.example.processo.core.segmento.veicular.domain.DadosVeiculares;
import com.example.processo.core.segmento.veicular.domain.ProcessosVeiculares;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessosVeicularesJpa extends ProcessosVeiculares {

    @Insert
    void inserir(ProcessoVeicularEntity processo);

    @Find
    Optional<ProcessoVeicularEntity> buscar(UUID id);

    @Override
    default void salvar(Processo<DadosVeiculares> p) {
        var veiculo = p.dados();
        inserir(new ProcessoVeicularEntity(p.id(), p.numero(), p.proponente().nome(), p.proponente().cpf(),
                p.valor(), SituacaoProcesso.valueOf(p.situacao().name()), p.dataAbertura(),
                veiculo.placa(), veiculo.anoFabricacao(), veiculo.valorFipe()));
    }

    @Override
    default Optional<Processo<DadosVeiculares>> porId(UUID id) {
        return buscar(id).map(e -> new Processo<>(e.getId(), e.getNumero(), e.getSegmento(),
                new Proponente(e.getProponenteNome(), e.getProponenteCpf()), e.getValor(),
                Situacao.valueOf(e.getSituacao().name()), e.getMotivoRecusa(), e.getDataAbertura(),
                new DadosVeiculares(e.getPlaca(), e.getAnoFabricacao(), e.getValorFipe())));
    }
}
