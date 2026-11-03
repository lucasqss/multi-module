package com.example.consorcio.adapter.cota;

import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.repository.CotaRepository;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter HIPOTÉTICO para o sistema dono das cotas.
 * Na vida real seria um REST Client / IIB chamando o legado e convertendo o DTO externo em {@link Cota}.
 *
 * <p>Massa fake (grupo 1234):
 * <ul>
 *   <li>cotas 1..49 — ATIVA, saldo devedor R$ 15.000</li>
 *   <li>cota 90 — INADIMPLENTE, saldo R$ 8.000</li>
 *   <li>cota 91 — CANCELADA</li>
 *   <li>cota 92 — ENCERRADA</li>
 * </ul>
 */
@ApplicationScoped
public class CotaRepositoryStub implements CotaRepository {

    public static final int GRUPO = 1234;

    private static final Map<IdentificadorCota, Cota> COTAS = new HashMap<>();

    static {
        for (int numero = 1; numero < 50; numero++) {
            adicionar(numero, SituacaoCota.ATIVA, "15000.00");
        }
        adicionar(90, SituacaoCota.INADIMPLENTE, "8000.00");
        adicionar(91, SituacaoCota.CANCELADA, "5000.00");
        adicionar(92, SituacaoCota.ENCERRADA, "0.00");
    }

    private static void adicionar(int numero, SituacaoCota situacao, String saldo) {
        var id = new IdentificadorCota(GRUPO, numero);
        COTAS.put(id, new Cota(id, situacao, Dinheiro.de(saldo)));
    }

    @Override
    public Optional<Cota> buscar(IdentificadorCota identificador) {
        return Optional.ofNullable(COTAS.get(identificador));
    }
}
