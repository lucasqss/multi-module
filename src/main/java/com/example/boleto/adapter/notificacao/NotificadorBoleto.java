package com.example.boleto.adapter.notificacao;

import com.example.boleto.domain.entity.Boleto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notificação ao pagador, injetada DIRETAMENTE no use case (sem interface).
 *
 * <p>SIMULAÇÃO: na vida real seria um cliente de e-mail/SMS/fila. Aqui só escreve no log e guarda o
 * que "enviou" (útil para testes e para ver a simulação). Detalhes do sistema externo (DTOs, URLs,
 * erros técnicos) ficam nesta classe; quem chama só conhece o {@link Boleto}.
 */
@ApplicationScoped
public class NotificadorBoleto {

    private static final Logger LOG = Logger.getLogger(NotificadorBoleto.class);

    private final List<NotificacaoEnviada> enviadas = new CopyOnWriteArrayList<>();

    /** Avisa o pagador e os destinatários extras. Só chamado para boleto emitido (com linha digitável). */
    public void notificarEmissao(Boleto boleto) {
        var linha = boleto.getLinhaDigitavel()
                .orElseThrow(() -> new IllegalStateException("Boleto sem linha digitável: " + boleto.getId()))
                .valor();
        var para = new ArrayList<String>();
        para.add(boleto.getPagador().email().valor());
        boleto.getDestinatarios().emails().forEach(e -> para.add(e.valor()));
        for (var destinatario : para) {
            enviadas.add(new NotificacaoEnviada(destinatario, linha));
            LOG.infof("[SIMULAÇÃO] E-mail para %s: boleto de %s vence em %s. Linha digitável: %s",
                    destinatario, boleto.getValor(), boleto.getDataVencimento(), linha);
        }
    }

    public List<NotificacaoEnviada> enviadas() {
        return List.copyOf(enviadas);
    }

    public record NotificacaoEnviada(String destinatario, String linhaDigitavel) {
    }
}
