package br.com.consorcio.utilizacao.dominio.nucleo.documento.aplicacao;

import br.com.consorcio.utilizacao.dominio.nucleo.documento.api.DocumentoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo.Documento;
import br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo.DocumentoRepository;
import br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo.RepositorioArquivos;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DocumentoAplicacao implements DocumentoApi {

    @Inject
    ProcessoApi processos;

    @Inject
    RepositorioArquivos ged;

    @Inject
    DocumentoRepository documentos;

    /**
     * Confere a pertença antes de tudo (senão daria para anexar documento a outro processo).
     * Grava no GED antes da transação: se o GED falhar, nada fica aqui; se o banco falhar depois,
     * sobra um arquivo órfão no GED, inofensivo.
     */
    @Override
    public Long enviar(EnviarDocumento cmd) {
        processos.exigirPertenca(cmd.processoId(), cmd.objetoId(), cmd.participacaoId());
        String idGed = ged.guardar(cmd.nomeArquivo(), cmd.mime(), cmd.conteudo());
        return registrar(cmd, idGed);
    }

    @Transactional
    Long registrar(EnviarDocumento cmd, String idGed) {
        Long donoProcesso = cmd.objetoId() == null && cmd.participacaoId() == null ? cmd.processoId() : null;
        Documento d = new Documento(cmd.tipoDocumento(), cmd.nomeArquivo(), cmd.mime(), idGed,
                cmd.objetoId(), cmd.participacaoId(), donoProcesso, null);
        documentos.persist(d);
        return d.id();
    }
}
