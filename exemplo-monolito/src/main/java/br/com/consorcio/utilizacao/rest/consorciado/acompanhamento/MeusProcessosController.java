package br.com.consorcio.utilizacao.rest.consorciado.acompanhamento;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi.LinhaDoTempo;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mesma LinhaDoTempo do backoffice, outra forma de tela: menos detalhe, linguagem do consorciado.
 * Em produção o CPF vem do token, não de um cabeçalho.
 */
@Path("/consorciado/processos")
public class MeusProcessosController {

    public record MeuProcesso(Long id, String estado, long fasesConcluidas, int totalFases, BigDecimal creditoReservado,
                              boolean garantiaConstituida) {

        static MeuProcesso de(LinhaDoTempo l) {
            var p = l.processo();
            return new MeuProcesso(p.id(), p.estado().name(), p.fasesResolvidas(), p.etapas().size(),
                    l.cotas().stream().map(c -> c.reservado()).reduce(BigDecimal.ZERO, BigDecimal::add),
                    !l.garantias().isEmpty() && l.garantias().stream().allMatch(g -> "CONSTITUIDA".equals(g.situacao())));
        }
    }

    @Inject
    ProcessoApi processos;

    @GET
    public List<MeuProcesso> meus(@HeaderParam("X-Cpf") String cpf) {
        return processos.doTitular(cpf).stream().map(MeuProcesso::de).toList();
    }
}
