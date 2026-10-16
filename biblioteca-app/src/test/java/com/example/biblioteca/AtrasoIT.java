package com.example.biblioteca;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.biblioteca.Api.emprestar;
import static com.example.biblioteca.Api.emprestimo;
import static com.example.biblioteca.Api.exemplar;
import static com.example.biblioteca.Api.leitor;
import static com.example.biblioteca.Api.livro;
import static com.example.biblioteca.Api.unico;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Único ponto que não passa só pela API: ela não permite avançar o relógio da aplicação, então o empréstimo é
 * criado pela API e a data prevista é recuada direto no banco (o mesmo Oracle do Dev Services que a aplicação usa).
 */
@QuarkusTest
class AtrasoIT {

    static final int DIAS_DE_ATRASO = 5;

    @Inject
    DataSource banco;

    @Test
    void atrasoApareceNoRelatorioEDevolucaoSuspendeLeitor() throws SQLException {
        var leitor = leitor();
        var exemplar = exemplar(livro("O Cortiço " + unico(), List.of("Aluísio Azevedo")));
        var emprestimo = emprestimo(leitor, exemplar);
        recuarDataPrevista(emprestimo, DIAS_DE_ATRASO);

        given().get("/emprestimos/atrasados").then()
                .statusCode(200)
                .body("findAll { it.emprestimoId == '" + emprestimo + "' }.diasDeAtraso", hasItem(DIAS_DE_ATRASO))
                .body("findAll { it.emprestimoId == '" + emprestimo + "' }.citacao", hasItem("(AZEVEDO, 1899)"));
        given().get("/leitores/" + leitor + "/painel").then().body("emprestimos[0].atrasado", equalTo(true));
        Api.post("/emprestimos/" + emprestimo + "/renovacao").statusCode(422);

        Api.post("/emprestimos/" + emprestimo + "/devolucao").statusCode(204);

        var suspensoAte = LocalDate.now().plusDays(DIAS_DE_ATRASO).toString();
        given().get("/leitores/" + leitor + "/painel").then().body("suspensoAte", equalTo(suspensoAte));
        emprestar(leitor, exemplar).statusCode(422).body("motivo", equalTo("Leitor suspenso até " + suspensoAte));
    }

    @Test
    void leitorComEmprestimoAtrasadoNaoPodeEmprestar() throws SQLException {
        var leitor = leitor();
        var livro = livro("A Moreninha " + unico(), List.of("Joaquim Manuel de Macedo"));
        recuarDataPrevista(emprestimo(leitor, exemplar(livro)), 1);

        emprestar(leitor, exemplar(livro)).statusCode(422)
                .body("motivo", equalTo("Leitor possui empréstimo atrasado"));
    }

    private void recuarDataPrevista(String emprestimoId, int dias) throws SQLException {
        try (var conexao = banco.getConnection();
             var update = conexao.prepareStatement("UPDATE EMPRESTIMO SET DATA_PREVISTA = ? WHERE ID = ?")) {
            update.setObject(1, LocalDate.now().minusDays(dias));
            update.setBytes(2, bytes(UUID.fromString(emprestimoId)));
            if (update.executeUpdate() != 1) {
                throw new IllegalStateException("Empréstimo " + emprestimoId + " não encontrado no banco");
            }
        }
    }

    /** RAW(16) no Oracle: os 16 bytes do UUID, mais e menos significativos. */
    private static byte[] bytes(UUID uuid) {
        var buffer = java.nio.ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
