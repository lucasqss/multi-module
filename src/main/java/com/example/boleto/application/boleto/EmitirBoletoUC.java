package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.specification.boleto.CondicoesEmissao;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.util.UUID;

/**
 * FINALIZA A EDIÇÃO: EM_EDICAO -> EMITIDO (simula a criação do boleto: gera a linha digitável).
 * É aqui, e só aqui, que a COMPLETUDE é exigida. O use case ORQUESTRA, não decide:
 * <ol>
 *   <li>carrega o boleto;</li>
 *   <li>busca os FATOS que as regras precisam (quantos boletos em aberto o pagador tem?);</li>
 *   <li>delega a DECISÃO ao domínio (specifications de emissão rodam dentro de {@code Boleto.emitir});</li>
 *   <li>persiste e devolve a saída, tudo numa transação.</li>
 * </ol>
 * Repare que não há nenhum {@code if} de regra de negócio aqui.
 */
@ApplicationScoped
public class EmitirBoletoUC {

    private final BoletoRepository repository;
    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public EmitirBoletoUC(BoletoRepository repository, Clock clock,
                          @ConfigProperty(name = "boleto.prazo-maximo-vencimento-dias") int prazoMaximoVencimentoDias) {
        this.repository = repository;
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    @Transactional
    public BoletoOutput executar(UUID id) {
        var boleto = repository.obter(new BoletoId(id));
        var emAberto = repository.contarEmAbertoPorPagador(boleto.getPagador().documento());

        boleto.emitir(CondicoesEmissao.de(clock, prazoMaximoVencimentoDias, emAberto));

        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
