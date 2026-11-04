package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.adapter.emissor.EmissorBoletos;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.application.pagamento.dto.SalvarBoletosInput;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.PagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Salva a SEÇÃO de boletos inteira (PUT). O usuário informa só as linhas digitáveis; os DADOS do boleto
 * (valor, vencimento, beneficiário) vêm do SISTEMA EMISSOR, que é o dono do boleto.
 *
 * <p>Um caso de uso por seção, não um por boleto: a intenção é "definir os boletos deste pagamento".
 */
@ApplicationScoped
public class SalvarBoletosDoPagamentoUC {

    private final PagamentoRepository repository;
    private final EmissorBoletos emissor;

    public SalvarBoletosDoPagamentoUC(PagamentoRepository repository, EmissorBoletos emissor) {
        this.repository = repository;
        this.emissor = emissor;
    }

    @Transactional
    public PagamentoOutput executar(SalvarBoletosInput input) {
        var pagamento = repository.obterParaAlteracao(new PagamentoId(input.id()));
        var boletos = new BoletosDoPagamento(input.linhas().stream().map(emissor::consultar).toList());

        pagamento.salvarBoletos(boletos);

        repository.salvar(pagamento);
        return PagamentoOutput.de(pagamento);
    }
}
