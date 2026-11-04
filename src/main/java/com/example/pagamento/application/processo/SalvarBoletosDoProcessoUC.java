package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.adapter.emissor.EmissorBoletos;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.application.processo.dto.SalvarBoletosInput;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Salva a SEÇÃO de boletos inteira (PUT). O usuário informa só as linhas digitáveis; os DADOS do boleto
 * (valor, vencimento, beneficiário) vêm do SISTEMA EMISSOR, que é o dono do boleto.
 *
 * <p>Um caso de uso por seção, não um por boleto: a intenção é "definir os boletos deste pagamento".
 */
@ApplicationScoped
public class SalvarBoletosDoProcessoUC {

    private final ProcessoPagamentoRepository repository;
    private final EmissorBoletos emissor;

    public SalvarBoletosDoProcessoUC(ProcessoPagamentoRepository repository, EmissorBoletos emissor) {
        this.repository = repository;
        this.emissor = emissor;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(SalvarBoletosInput input) {
        var processo = repository.obter(new ProcessoPagamentoId(input.id()));
        var boletos = new BoletosDoProcesso(input.linhas().stream().map(emissor::consultar).toList()); // fatos do emissor

        processo.salvarBoletos(boletos);

        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
