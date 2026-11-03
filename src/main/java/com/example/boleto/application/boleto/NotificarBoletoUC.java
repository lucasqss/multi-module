package com.example.boleto.application.boleto;

import com.example.boleto.application.boleto.dto.BoletoOutput;
import com.example.boleto.domain.gateway.NotificadorBoleto;
import com.example.boleto.domain.repository.BoletoRepository;
import com.example.boleto.domain.valueobject.BoletoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Simula a NOTIFICAÇÃO de emissão ao pagador. EMITIDO -> NOTIFICADO (ou reenvio, se já NOTIFICADO).
 *
 * <p>A ORDEM importa: (1) o domínio valida a transição (boleto pago/cancelado não é notificado);
 * (2) salva, o que detecta concorrência antes de qualquer efeito externo; (3) só então envia.
 * Se o envio falhar, a exceção desfaz a transação. Ainda resta a janela "enviou, mas o commit falhou";
 * em produção, isso se resolve com o padrão Outbox (gravar a notificação na mesma transação e
 * enviar depois, de forma assíncrona).
 */
@ApplicationScoped
public class NotificarBoletoUC {

    private final BoletoRepository repository;
    private final NotificadorBoleto notificador;
    private final Clock clock;

    public NotificarBoletoUC(BoletoRepository repository, NotificadorBoleto notificador, Clock clock) {
        this.repository = repository;
        this.notificador = notificador;
        this.clock = clock;
    }

    @Transactional
    public BoletoOutput executar(UUID id) {
        var boleto = repository.obter(new BoletoId(id));
        boleto.registrarNotificacao(clock.instant());
        repository.salvar(boleto);

        notificador.notificarEmissao(boleto);
        return BoletoOutput.de(boleto);
    }
}
