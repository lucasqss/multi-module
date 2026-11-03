package com.example.consorcio.adapter.cota;

import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;

/**
 * Adapter HIPOTÉTICO para o sistema dono das cotas.
 * Na vida real seria um REST Client / IIB chamando o sistema legado e convertendo
 * a resposta (DTO externo) em {@link Cota}. Aqui devolve dados fixos para o exemplo.
 */
@ApplicationScoped
public class CotaRepositoryStub implements CotaRepository {

    private static final Map<IdentificadorCota, Cota> COTAS = Map.of(
            new IdentificadorCota(1234, 10),
            new Cota(new IdentificadorCota(1234, 10), SituacaoCota.ATIVA, Dinheiro.de("15000.00")),
            new IdentificadorCota(1234, 11),
            new Cota(new IdentificadorCota(1234, 11), SituacaoCota.INADIMPLENTE, Dinheiro.de("8000.00")),
            new IdentificadorCota(1234, 12),
            new Cota(new IdentificadorCota(1234, 12), SituacaoCota.CANCELADA, Dinheiro.de("5000.00")));

    @Override
    public Optional<Cota> buscar(IdentificadorCota identificador) {
        return Optional.ofNullable(COTAS.get(identificador));
    }
}
