package com.example.pagamento.domain.entity;

import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import com.example.pagamento.domain.valueobject.RetornoBanco;
import com.example.pagamento.domain.valueobject.StatusTentativa;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * ENTIDADE INTERNA do {@link ProcessoPagamento}: cada envio ao banco é uma tentativa, com identidade
 * (número dentro do processo / id da transação) e um mini ciclo de vida próprio (ENVIADA -> LIQUIDADA|RECUSADA).
 * Só o processo cria e conclui tentativas; não há repositório nem use case por tentativa.
 */
public final class TentativaPagamento {

    private final int numero;
    private final String idTransacao;
    private final Instant enviadaEm;
    private StatusTentativa status;
    private Dinheiro valorLiquidado;
    private String codigoAutenticacao;
    private String motivoRecusa;
    private Instant concluidaEm;

    private TentativaPagamento(int numero, String idTransacao, Instant enviadaEm, StatusTentativa status,
                               Dinheiro valorLiquidado, String codigoAutenticacao, String motivoRecusa,
                               Instant concluidaEm) {
        this.numero = numero;
        this.idTransacao = Objects.requireNonNull(idTransacao);
        this.enviadaEm = Objects.requireNonNull(enviadaEm);
        this.status = Objects.requireNonNull(status);
        this.valorLiquidado = valorLiquidado;
        this.codigoAutenticacao = codigoAutenticacao;
        this.motivoRecusa = motivoRecusa;
        this.concluidaEm = concluidaEm;
    }

    /** O id da transação é nosso: vai ao banco no envio e volta no retorno. */
    static TentativaPagamento nova(ProcessoPagamentoId processo, int numero, Instant agora) {
        return new TentativaPagamento(numero, processo + "-" + numero, agora, StatusTentativa.ENVIADA,
                null, null, null, null);
    }

    public static TentativaPagamento reconstituir(int numero, String idTransacao, Instant enviadaEm,
                                                  StatusTentativa status, Dinheiro valorLiquidado,
                                                  String codigoAutenticacao, String motivoRecusa, Instant concluidaEm) {
        return new TentativaPagamento(numero, idTransacao, enviadaEm, status, valorLiquidado,
                codigoAutenticacao, motivoRecusa, concluidaEm);
    }

    void concluir(RetornoBanco retorno) {
        if (!status.aberta()) {
            throw new IllegalStateException("Tentativa %s já concluída".formatted(idTransacao));
        }
        status = retorno.liquidado() ? StatusTentativa.LIQUIDADA : StatusTentativa.RECUSADA;
        valorLiquidado = retorno.valorLiquidado();
        codigoAutenticacao = retorno.codigoAutenticacao();
        motivoRecusa = retorno.motivoRecusa();
        concluidaEm = retorno.momento();
    }

    public boolean aberta() { return status.aberta(); }

    public int getNumero() { return numero; }
    public String getIdTransacao() { return idTransacao; }
    public Instant getEnviadaEm() { return enviadaEm; }
    public StatusTentativa getStatus() { return status; }
    public Optional<Dinheiro> getValorLiquidado() { return Optional.ofNullable(valorLiquidado); }
    public Optional<String> getCodigoAutenticacao() { return Optional.ofNullable(codigoAutenticacao); }
    public Optional<String> getMotivoRecusa() { return Optional.ofNullable(motivoRecusa); }
    public Optional<Instant> getConcluidaEm() { return Optional.ofNullable(concluidaEm); }
}
