package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.EmitirBoletoInput;
import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.specification.boleto.SolicitacaoBoleto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;

/**
 * CREATE: simula a criação do boleto (gera a linha digitável). O use case ORQUESTRA, não decide:
 * <ol>
 *   <li>converte a entrada em objetos de domínio;</li>
 *   <li>busca os FATOS que as regras precisam (quantos boletos em aberto o pagador tem?);</li>
 *   <li>delega a DECISÃO ao domínio (specifications rodam dentro de {@code Boleto.emitir});</li>
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
    public BoletoOutput executar(EmitirBoletoInput input) {
        var dados = input.dadosBoleto();
        var emAberto = repository.contarEmAbertoPorPagador(dados.pagador().documento());

        var boleto = Boleto.emitir(SolicitacaoBoleto.paraEmissao(dados, clock, prazoMaximoVencimentoDias, emAberto));

        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
