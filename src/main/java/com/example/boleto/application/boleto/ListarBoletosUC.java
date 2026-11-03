package com.example.boleto.application.boleto;

import com.example.boleto.adapter.database.BoletoRepository;
import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.application.boleto.dto.ListarBoletosInput;
import com.example.boleto.shared.utils.Pagina;
import jakarta.enterprise.context.ApplicationScoped;

/** READ (lista paginada com filtros). */
@ApplicationScoped
public class ListarBoletosUC {

    private final BoletoRepository repository;

    public ListarBoletosUC(BoletoRepository repository) {
        this.repository = repository;
    }

    public Pagina<BoletoOutput> executar(ListarBoletosInput input) {
        return repository.listar(input.filtro(), input.pagina(), input.tamanho()).map(BoletoOutput::de);
    }
}
