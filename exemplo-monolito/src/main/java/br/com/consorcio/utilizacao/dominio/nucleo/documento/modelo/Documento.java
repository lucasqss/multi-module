package br.com.consorcio.utilizacao.dominio.nucleo.documento.modelo;

import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.stream.Stream;

/** Metadados; o arquivo mora no GED. Dono exclusivo, como no CHECK ck_doc_dono_unico. */
@Entity
@Table(name = "doc_documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "id_ged")
    private String idGed;

    @Column(name = "nome_arquivo")
    private String nomeArquivo;

    private String mime;

    /** ENVIADO, EM_CONFERENCIA, ACEITO, RECUSADO, VENCIDO */
    private String situacao = "ENVIADO";

    @Column(name = "objeto_id")
    private Long objetoId;

    @Column(name = "participacao_id")
    private Long participacaoId;

    @Column(name = "processo_id")
    private Long processoId;

    @Column(name = "instrumento_id")
    private Long instrumentoId;

    protected Documento() {
    }

    public Documento(String tipoDocumento, String nomeArquivo, String mime, String idGed,
                     Long objetoId, Long participacaoId, Long processoId, Long instrumentoId) {
        long donos = Stream.of(objetoId, participacaoId, processoId, instrumentoId).filter(Objects::nonNull).count();
        RegraNegocioException.exigir(donos == 1, "DONO_DOCUMENTO", "Documento precisa de exatamente um dono");
        this.tipoDocumento = tipoDocumento;
        this.nomeArquivo = nomeArquivo;
        this.mime = mime;
        this.idGed = idGed;
        this.objetoId = objetoId;
        this.participacaoId = participacaoId;
        this.processoId = processoId;
        this.instrumentoId = instrumentoId;
    }

    public Long id() { return id; }
}
