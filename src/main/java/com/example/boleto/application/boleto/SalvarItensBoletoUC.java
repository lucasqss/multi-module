package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.SalvarItensBoletoInput;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Salva a SEÇÃO de itens inteira (PUT). Um caso de uso por seção, e não um por operação de item
 * (incluir/alterar/remover): a intenção do usuário é "salvar os itens deste boleto".
 * Os itens são partes do boleto: carrega o boleto, o boleto aplica as regras, salva o boleto.
 */
@ApplicationScoped
public class SalvarItensBoletoUC {

    private final BoletoRepository repository;

    public SalvarItensBoletoUC(BoletoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletoOutput executar(SalvarItensBoletoInput input) {
        var boleto = repository.obter(new BoletoId(input.id()));
        boleto.salvarItens(input.itensBoleto());
        repository.salvar(boleto);
        return BoletoOutput.de(boleto);
    }
}
