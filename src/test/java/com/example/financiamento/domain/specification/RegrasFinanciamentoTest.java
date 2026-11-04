package com.example.financiamento.domain.specification;

import com.example.financiamento.domain.Cenarios;
import com.example.financiamento.domain.exception.CondicoesNaoInformadasException;
import com.example.financiamento.domain.exception.DocumentoObrigatorioAusenteException;
import com.example.financiamento.domain.exception.DocumentoRepetidoException;
import com.example.financiamento.domain.exception.GarantiaNaoInformadaException;
import com.example.financiamento.domain.exception.ParcelaAcimaDaRendaException;
import com.example.financiamento.domain.exception.PrazoInvalidoException;
import com.example.financiamento.domain.exception.ProponenteNaoInformadoException;
import com.example.financiamento.domain.exception.ValorAcimaDoPercentualException;
import com.example.financiamento.domain.exception.VeiculoMuitoAntigoException;
import com.example.financiamento.domain.specification.financiamento.RegrasFinanciamento;
import com.example.financiamento.domain.valueobject.Documento;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.TipoDocumento;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** As regras do catálogo, organizadas por ELO da cadeia de submissão. */
class RegrasFinanciamentoTest {

    private static void submeter(Cenarios c) {
        RegrasFinanciamento.SUBMISSAO.validar(c.submissao());
    }

    @Test
    void secaoDocumentosNaoAceitaRepetido() {
        var d = new Documento(TipoDocumento.IDENTIDADE, "RG-1");
        assertThrows(DocumentoRepetidoException.class,
                () -> RegrasFinanciamento.DOCUMENTOS.validar(new DocumentosDoFinanciamento(List.of(d, d))));
    }

    @Nested
    class ComunsAosDoisTipos {

        @Test
        void exigemProponenteCondicoesEGarantia() {
            assertThrows(ProponenteNaoInformadoException.class, () -> submeter(Cenarios.imovel().semProponente()));
            assertThrows(CondicoesNaoInformadasException.class, () -> submeter(Cenarios.veiculo().semCondicoes()));
            assertThrows(GarantiaNaoInformadaException.class, () -> submeter(Cenarios.imovel().bem(null)));
        }

        @Test
        void exigemIdentidadeEComprovanteDeRenda() {
            var e = assertThrows(DocumentoObrigatorioAusenteException.class,
                    () -> submeter(Cenarios.veiculo().semDocumento(TipoDocumento.COMPROVANTE_RENDA)));
            assertEquals("Documento obrigatório ausente: COMPROVANTE_RENDA", e.getMessage());
        }

        @Test
        void parcelaAteTrintaPorCentoDaRenda() {
            // renda 10.000 -> parcela máxima 3.000; 72.000 em 24 meses = 3.000
            assertDoesNotThrow(() -> submeter(Cenarios.veiculo().valor("72000.00").prazo(24)));
            assertThrows(ParcelaAcimaDaRendaException.class, () -> submeter(Cenarios.veiculo().valor("72001.00").prazo(24)));
        }
    }

    @Nested
    class Imovel {

        @Test
        void prazoDe60A420Meses() {
            assertDoesNotThrow(() -> submeter(Cenarios.imovel().prazo(420)));
            assertThrows(PrazoInvalidoException.class, () -> submeter(Cenarios.imovel().prazo(421)));
            assertThrows(PrazoInvalidoException.class, () -> submeter(Cenarios.imovel().valor("10000.00").prazo(59)));
        }

        @Test
        void financiaAteOitentaPorCentoDoImovel() {
            assertDoesNotThrow(() -> submeter(Cenarios.imovel().valor("400000.00")));
            assertThrows(ValorAcimaDoPercentualException.class, () -> submeter(Cenarios.imovel().valor("400000.01")));
        }

        @Test
        void exigeMatricula() {
            assertThrows(DocumentoObrigatorioAusenteException.class,
                    () -> submeter(Cenarios.imovel().semDocumento(TipoDocumento.MATRICULA_IMOVEL)));
        }
    }

    @Nested
    class Veiculo {

        @Test
        void prazoDe12A60Meses() {
            assertDoesNotThrow(() -> submeter(Cenarios.veiculo().prazo(60)));
            assertThrows(PrazoInvalidoException.class, () -> submeter(Cenarios.veiculo().prazo(61)));
            assertThrows(PrazoInvalidoException.class, () -> submeter(Cenarios.veiculo().valor("10000.00").prazo(11)));
        }

        @Test
        void financiaAteNoventaPorCentoDoVeiculo() {
            assertDoesNotThrow(() -> submeter(Cenarios.veiculo().valor("90000.00").prazo(60)));
            assertThrows(ValorAcimaDoPercentualException.class, () -> submeter(Cenarios.veiculo().valor("90000.01").prazo(60)));
        }

        @Test
        void exigeCrlvEVeiculoDeAteDezAnos() {
            assertThrows(DocumentoObrigatorioAusenteException.class,
                    () -> submeter(Cenarios.veiculo().semDocumento(TipoDocumento.CRLV)));
            assertDoesNotThrow(() -> submeter(Cenarios.veiculo().bem(Cenarios.veiculoAno(2016))));
            assertThrows(VeiculoMuitoAntigoException.class, () -> submeter(Cenarios.veiculo().bem(Cenarios.veiculoAno(2015))));
        }
    }
}
