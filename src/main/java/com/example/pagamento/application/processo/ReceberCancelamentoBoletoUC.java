package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * O SISTEMA EMISSOR avisa que cancelou um boleto (chamado pelo webhook). É um FATO sobre algo que não
 * é nosso: cada processo que contém o boleto decide o que isso significa para ele (cancelar, se ainda não
 * foi ao banco; análise, se já foi). O laço abaixo só aplica o fato a cada agregado; não há regra aqui.
 */
@ApplicationScoped
public class ReceberCancelamentoBoletoUC {

    private final ProcessoPagamentoRepository repository;

    public ReceberCancelamentoBoletoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    /** @return quantos processos foram afetados (0 se nenhum processo nosso tem o boleto). */
    @Transactional
    public int executar(String idBoletoNoEmissor) {
        var processos = repository.listarComBoleto(idBoletoNoEmissor);
        for (var processo : processos) {
            processo.registrarBoletoCanceladoNoEmissor(idBoletoNoEmissor);
            repository.salvar(processo);
        }
        return processos.size();
    }
}
