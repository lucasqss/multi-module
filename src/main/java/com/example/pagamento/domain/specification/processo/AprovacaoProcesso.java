package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Matricula;

import java.util.List;

/** CANDIDATO das regras de APROVAÇÃO e da ALÇADA: quem pede, quem já aprovou, quem aprova agora, e o valor. */
public record AprovacaoProcesso(Matricula solicitante, List<Matricula> aprovadoresAnteriores, Matricula novoAprovador,
                                Dinheiro valorTotal) {

    public AprovacaoProcesso {
        aprovadoresAnteriores = List.copyOf(aprovadoresAnteriores);
    }

    /** Aprovações contando a que está sendo registrada agora. */
    public int totalDeAprovacoes() {
        return aprovadoresAnteriores.size() + 1;
    }
}
