# Modelos de código

Arquivos completos do projeto de referência (pacote `com.example.processo`), que compilam e passam nos testes. Troque o pacote e os nomes do domínio; mantenha a forma. O porquê de cada um está no `GUIA.md`.

> Gerado por `gerar-modelos.py` a partir do código do repositório de referência. Não edite à mão: mude o código
> de lá e rode o script de novo.

## Índice

1. [Modelo: entidades](#1-modelo-entidades)
2. [Modelo: estados (State em enum)](#2-modelo-estados-state-em-enum)
3. [Modelo: value objects e exceções comuns](#3-modelo-value-objects-e-exceções-comuns)
4. [Negócio: regras](#4-negócio-regras)
5. [Negócio: ponto de extensão e implementações](#5-negócio-ponto-de-extensão-e-implementações)
6. [Negócio: o motor](#6-negócio-o-motor)
7. [Negócio: CRUD pelo caminho curto](#7-negócio-crud-pelo-caminho-curto)
8. [Persistência: repositories](#8-persistência-repositories)
9. [Persistência: consultas para as telas](#9-persistência-consultas-para-as-telas)
10. [Integração com sistema externo](#10-integração-com-sistema-externo)
11. [Apresentação: REST](#11-apresentação-rest)
12. [Apresentação: fila](#12-apresentação-fila)
13. [package-info de uma camada](#13-package-info-de-uma-camada)
14. [Migrations](#14-migrations)
15. [Configuração](#15-configuração)
16. [Testes puros](#16-testes-puros)
17. [Testes com o banco (@QuarkusTest, *BancoTest)](#17-testes-com-o-banco-quarkustest-bancotest)
18. [Infraestrutura de teste](#18-infraestrutura-de-teste)
19. [Testes de integração (contra o jar)](#19-testes-de-integração-contra-o-jar)

## 1. Modelo: entidades

A entidade pai (herança JOINED, discriminador só leitura, construtor de pedido, eventos de estado) e uma filha. A filha declara a constante do discriminador e preenche o pai pelo construtor.

### `src/main/java/com/example/processo/modelo/Processo.java`

```java
package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Processo de qualquer modalidade: os dados comuns ficam na tabela PROCESSO. Cada modalidade é uma subclasse com a
 * própria tabela (herança JOINED, ligada pela PK); a coluna SEGMENTO (discriminador) diz qual filha completa a linha.
 *
 * <p>É também o Context do padrão State: guarda a {@link Situacao} e delega cada evento a ela, que decide se a
 * transição vale. Quem grava a nova situação é o {@code ProcessoService}, com compare-and-set.
 *
 * <p>Nasce como pedido de abertura (sem id, número nem data), passa pelas regras e só então é aberto e gravado.
 */
@Entity
@Table(name = "PROCESSO")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "SEGMENTO", length = 30)
public abstract class Processo {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    /** O discriminador. Quem grava é o Hibernate, pelo @DiscriminatorValue da filha; aqui é só para leitura. */
    @Column(name = "SEGMENTO", insertable = false, updatable = false)
    private String segmento;

    @Column(name = "PROPONENTE_NOME", nullable = false)
    private String proponenteNome;

    @Column(name = "PROPONENTE_CPF", nullable = false, length = 11)
    private String proponenteCpf;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Situacao situacao;

    /** Preenchido só quando a situação é RECUSADO. */
    @Column(name = "MOTIVO_RECUSA")
    private String motivoRecusa;

    @Column(name = "DATA_ABERTURA", nullable = false)
    private LocalDate dataAbertura;

    protected Processo() {
    }

    /**
     * Pedido de abertura. CPF e valor chegam já válidos (value objects) ou nulos; campos ausentes seguem nulos para as
     * regras recusarem com a mensagem certa.
     */
    protected Processo(String segmento, String proponenteNome, Cpf cpf, Valor valor) {
        this.segmento = segmento;
        this.proponenteNome = proponenteNome;
        this.proponenteCpf = cpf == null ? null : cpf.numero();
        this.valor = valor == null ? null : valor.quantia();
    }

    /** Depois das regras: ganha id, número e data, e nasce ABERTO. */
    public void abrir(String numero, LocalDate hoje) {
        this.id = UUID.randomUUID();
        this.numero = numero;
        this.dataAbertura = hoje;
        this.situacao = Situacao.ABERTO;
    }

    public void iniciarProcessamento() {
        situacao = situacao.iniciarProcessamento();
    }

    public void concluir() {
        situacao = situacao.concluir();
    }

    public void recusar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Recusa exige motivo");
        }
        situacao = situacao.recusar();
        motivoRecusa = motivo;
    }

    public void cancelar() {
        situacao = situacao.cancelar();
    }

    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public String getSegmento() { return segmento; }
    public String getProponenteNome() { return proponenteNome; }
    public String getProponenteCpf() { return proponenteCpf; }
    public BigDecimal getValor() { return valor; }
    public Situacao getSituacao() { return situacao; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public LocalDate getDataAbertura() { return dataAbertura; }
}
```

### `src/main/java/com/example/processo/modelo/ProcessoVeicular.java`

```java
package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Processo da modalidade veicular: gravá-lo insere a linha em PROCESSO e em PROCESSO_VEICULAR. */
@Entity
@Table(name = "PROCESSO_VEICULAR")
@DiscriminatorValue(ProcessoVeicular.SEGMENTO)
@PrimaryKeyJoinColumn(name = "PROCESSO_ID")
public class ProcessoVeicular extends Processo {

    public static final String SEGMENTO = "VEICULAR";

    @Column(nullable = false, length = 7)
    private String placa;

    @Column(name = "ANO_FABRICACAO", nullable = false)
    private Integer anoFabricacao;

    @Column(name = "VALOR_FIPE", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFipe;

    protected ProcessoVeicular() {
    }

    public ProcessoVeicular(String proponenteNome, Cpf cpf, Valor valor,
                            String placa, Integer anoFabricacao, BigDecimal valorFipe) {
        super(SEGMENTO, proponenteNome, cpf, valor);
        this.placa = placa;
        this.anoFabricacao = anoFabricacao;
        this.valorFipe = valorFipe;
    }

    public String getPlaca() { return placa; }
    public Integer getAnoFabricacao() { return anoFabricacao; }
    public BigDecimal getValorFipe() { return valorFipe; }
}
```

## 2. Modelo: estados (State em enum)

Cada constante sobrescreve só as transições que aceita.

### `src/main/java/com/example/processo/modelo/Situacao.java`

```java
package com.example.processo.modelo;

/**
 * Padrão State (GoF) num enum: cada situação do processo é um estado que sabe quais eventos aceita e para qual
 * situação cada um leva. O Context é o {@link Processo}, que delega a ela e não tem nenhum {@code if} sobre a
 * situação. A máquina é a mesma para todas as modalidades:
 *
 * <pre>
 * ABERTO ──iniciarProcessamento──► EM_PROCESSAMENTO ──concluir──► CONCLUIDO
 *    │                                    └───recusar(motivo)──► RECUSADO
 *    └──cancelar──► CANCELADO
 * </pre>
 *
 * Por padrão todo evento é inválido; cada constante sobrescreve só as transições que permite. O nome da constante
 * é o valor persistido (PROCESSO.SITUACAO) e serializado: constante nova pede também o CHECK numa
 * migration nova.
 */
public enum Situacao {

    /** Recém-criado: pode ir para processamento ou ser cancelado. */
    ABERTO {
        @Override
        public Situacao iniciarProcessamento() {
            return EM_PROCESSAMENTO;
        }

        @Override
        public Situacao cancelar() {
            return CANCELADO;
        }
    },

    /** A modalidade está avaliando: termina concluído ou recusado. Não pode mais ser cancelado. */
    EM_PROCESSAMENTO {
        @Override
        public Situacao concluir() {
            return CONCLUIDO;
        }

        @Override
        public Situacao recusar() {
            return RECUSADO;
        }
    },

    /** Finais: não aceitam nenhum evento. */
    CONCLUIDO, RECUSADO, CANCELADO;

    public Situacao iniciarProcessamento() {
        throw new TransicaoInvalidaException(this, "processado");
    }

    public Situacao concluir() {
        throw new TransicaoInvalidaException(this, "concluído");
    }

    public Situacao recusar() {
        throw new TransicaoInvalidaException(this, "recusado");
    }

    public Situacao cancelar() {
        throw new TransicaoInvalidaException(this, "cancelado");
    }
}
```

### `src/main/java/com/example/processo/modelo/TransicaoInvalidaException.java`

```java
package com.example.processo.modelo;

/** O estado atual não aceita o evento pedido. O motivo é apresentável ao usuário. */
public class TransicaoInvalidaException extends RuntimeException {

    TransicaoInvalidaException(Situacao atual, String acao) {
        super("Processo " + atual + " não pode ser " + acao);
    }
}
```

## 3. Modelo: value objects e exceções comuns

Válidos por construção; as exceções viram 422 e 503.

### `src/main/java/com/example/processo/modelo/Cpf.java`

```java
package com.example.processo.modelo;

/**
 * CPF válido por construção: 11 dígitos, não todos iguais, com os dois dígitos verificadores corretos. Quem recebe
 * um {@code Cpf} não precisa validar de novo; a validação acontece uma vez, onde o texto entra no domínio.
 */
public record Cpf(String numero) {

    public Cpf {
        if (!valido(numero)) {
            throw new RegraVioladaException("CPF inválido: " + numero);
        }
    }

    private static boolean valido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            if ((soma * 10) % 11 % 10 != cpf.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }

    /** Só os dígitos: é como o CPF aparece em mensagens e é gravado. */
    @Override
    public String toString() {
        return numero;
    }
}
```

### `src/main/java/com/example/processo/modelo/Valor.java`

```java
package com.example.processo.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetário do processo, válido por construção: maior que zero e com no máximo 2 casas decimais. A escala é
 * sempre 2, então {@code 400000} e {@code 400000.00} são o mesmo valor (o Oracle devolve sem os zeros à direita).
 */
public record Valor(BigDecimal quantia) {

    public Valor {
        if (quantia == null || quantia.signum() <= 0) {
            throw new RegraVioladaException("Valor do processo deve ser maior que zero");
        }
        try {
            quantia = quantia.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new RegraVioladaException("Valor deve ter no máximo 2 casas decimais");
        }
    }

    public Valor(String quantia) {
        this(new BigDecimal(quantia));
    }

    @Override
    public String toString() {
        return quantia.toPlainString();
    }
}
```

### `src/main/java/com/example/processo/modelo/RegraVioladaException.java`

```java
package com.example.processo.modelo;

/** Uma regra de negócio impediu a operação; o motivo é apresentável ao usuário. */
public class RegraVioladaException extends RuntimeException {

    public RegraVioladaException(String motivo) {
        super(motivo);
    }
}
```

### `src/main/java/com/example/processo/modelo/SistemaExternoIndisponivelException.java`

```java
package com.example.processo.modelo;

/**
 * Um sistema externo consultado pelo negócio não respondeu (fora do ar, erro, timeout). Lançada pela camada de
 * integração, que traduz os erros técnicos do cliente HTTP: o negócio e a apresentação não conhecem REST Client.
 */
public class SistemaExternoIndisponivelException extends RuntimeException {

    public SistemaExternoIndisponivelException(String sistema, Throwable causa) {
        super("Sistema " + sistema + " indisponível; tente novamente mais tarde", causa);
    }
}
```

## 4. Negócio: regras

Especificação composta, regras comuns e regras de um tipo.

### `src/main/java/com/example/processo/negocio/processo/Especificacao.java`

```java
package com.example.processo.negocio.processo;

import com.example.processo.modelo.RegraVioladaException;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Regra de negócio sobre um candidato. Devolve o motivo quando a regra é violada, para que uma composição de
 * regras diga exatamente qual falhou.
 */
@FunctionalInterface
public interface Especificacao<T> {

    Optional<String> violacao(T candidato);

    default Especificacao<T> e(Especificacao<? super T> outra) {
        return candidato -> violacao(candidato).or(() -> outra.violacao(candidato));
    }

    default void exigir(T candidato) {
        violacao(candidato).ifPresent(motivo -> {
            throw new RegraVioladaException(motivo);
        });
    }

    /** Atalho para as regras simples: viola com {@code motivo} quando {@code condicao} é falsa. */
    static <T> Especificacao<T> exige(Predicate<? super T> condicao, String motivo) {
        return candidato -> condicao.test(candidato) ? Optional.empty() : Optional.of(motivo);
    }
}
```

### `src/main/java/com/example/processo/negocio/processo/RegrasComuns.java`

```java
package com.example.processo.negocio.processo;

import com.example.processo.modelo.Processo;

/**
 * Regras que valem para todas as modalidades. O serviço as aplica antes das regras da modalidade. CPF e valor
 * inválidos nem chegam aqui: {@code Cpf} e {@code Valor} já nascem válidos; aqui só se confere que vieram.
 */
public final class RegrasComuns {

    private RegrasComuns() {
    }

    private static final Especificacao<Processo> NOME = Especificacao.exige(
            p -> p.getProponenteNome() != null && !p.getProponenteNome().isBlank(),
            "Nome do proponente é obrigatório");

    private static final Especificacao<Processo> VALOR = Especificacao.exige(
            p -> p.getValor() != null, "Valor do processo deve ser maior que zero");

    public static final Especificacao<Processo> TODAS = NOME.e(VALOR);
}
```

### `src/main/java/com/example/processo/negocio/veicular/RegrasVeiculares.java`

```java
package com.example.processo.negocio.veicular;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.negocio.processo.Especificacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.example.processo.negocio.processo.Especificacao.exige;

/** Regras da modalidade veicular na abertura. A idade do veículo é decidida no processamento. */
public final class RegrasVeiculares {

    /** Fração máxima da tabela FIPE que o valor do processo pode alcançar. */
    static final BigDecimal LIMITE_DA_FIPE = new BigDecimal("0.90");

    private RegrasVeiculares() {
    }

    /** Padrão antigo (ABC1234) ou Mercosul (ABC1D23). */
    private static final Especificacao<ProcessoVeicular> PLACA = exige(
            p -> p.getPlaca() != null && p.getPlaca().matches("[A-Z]{3}\\d[A-Z0-9]\\d{2}"),
            "Placa inválida");

    private static final Especificacao<ProcessoVeicular> ANO = exige(
            p -> p.getAnoFabricacao() != null && p.getAnoFabricacao() >= 1950,
            "Ano de fabricação inválido");

    private static final Especificacao<ProcessoVeicular> FIPE = exige(
            p -> p.getValorFipe() != null && p.getValorFipe().signum() > 0,
            "Valor FIPE deve ser maior que zero");

    private static final Especificacao<ProcessoVeicular> DENTRO_DO_LIMITE = p -> {
        var limite = p.getValorFipe().multiply(LIMITE_DA_FIPE).setScale(2, RoundingMode.DOWN);
        return p.getValor().compareTo(limite) <= 0
                ? Optional.empty()
                : Optional.of("Valor excede 90% da FIPE (máximo " + limite.toPlainString() + ")");
    };

    public static final Especificacao<ProcessoVeicular> TODAS = PLACA.e(ANO).e(FIPE).e(DENTRO_DO_LIMITE);
}
```

## 5. Negócio: ponto de extensão e implementações

A interface, a decisão do processamento e um tipo com regra que consulta o banco (por último na composição).

### `src/main/java/com/example/processo/negocio/processo/Modalidade.java`

```java
package com.example.processo.negocio.processo;

import com.example.processo.modelo.Processo;

/**
 * O que muda de uma modalidade de consórcio para outra. Cada modalidade é um bean que implementa esta interface; o
 * {@link ProcessoService} descobre todas e escolhe pela classe do processo. Modalidade nova não altera o serviço.
 */
public interface Modalidade<P extends Processo> {

    /** A entidade da modalidade (subclasse de {@link Processo}). */
    Class<P> tipo();

    /** Prefixo do número do processo. */
    String prefixo();

    /** Regras próprias da modalidade; o serviço só as aplica depois das {@link RegrasComuns}. */
    Especificacao<P> regras();

    /**
     * A decisão própria da modalidade no processamento. Roda fora de transação: pode consultar sistema externo. Os
     * estados e as transições são os mesmos para todas ({@code Situacao}); a modalidade só decide.
     */
    Avaliacao avaliar(P processo);
}
```

### `src/main/java/com/example/processo/negocio/processo/Avaliacao.java`

```java
package com.example.processo.negocio.processo;

/** Decisão da modalidade sobre um processo em processamento. O serviço aplica a transição correspondente. */
public record Avaliacao(boolean aprovada, String motivo) {

    public static Avaliacao aprovado() {
        return new Avaliacao(true, null);
    }

    public static Avaliacao recusado(String motivo) {
        return new Avaliacao(false, motivo);
    }
}
```

### `src/main/java/com/example/processo/negocio/veicular/ModalidadeVeicular.java`

```java
package com.example.processo.negocio.veicular;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.negocio.processo.Avaliacao;
import com.example.processo.negocio.processo.Especificacao;
import com.example.processo.negocio.processo.Modalidade;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.LocalDate;

/** A modalidade veicular no motor: basta ser um bean que implementa {@link Modalidade}. */
@ApplicationScoped
public class ModalidadeVeicular implements Modalidade<ProcessoVeicular> {

    /** Veículo mais velho que isso é recusado no processamento. */
    public static final int IDADE_MAXIMA = 10;

    private final Clock relogio;

    public ModalidadeVeicular(Clock relogio) {
        this.relogio = relogio;
    }

    @Override
    public Class<ProcessoVeicular> tipo() {
        return ProcessoVeicular.class;
    }

    @Override
    public String prefixo() {
        return "VEI";
    }

    @Override
    public Especificacao<ProcessoVeicular> regras() {
        return RegrasVeiculares.TODAS;
    }

    /** A idade conta na data do processamento, não na da abertura. */
    @Override
    public Avaliacao avaliar(ProcessoVeicular processo) {
        var idade = LocalDate.now(relogio).getYear() - processo.getAnoFabricacao();
        return idade > IDADE_MAXIMA
                ? Avaliacao.recusado("Veículo com mais de " + IDADE_MAXIMA + " anos (fabricado em "
                        + processo.getAnoFabricacao() + ")")
                : Avaliacao.aprovado();
    }
}
```

### `src/main/java/com/example/processo/negocio/imobiliario/ModalidadeImobiliaria.java`

```java
package com.example.processo.negocio.imobiliario;

import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.negocio.processo.Avaliacao;
import com.example.processo.negocio.processo.Especificacao;
import com.example.processo.negocio.processo.Modalidade;
import com.example.processo.persistencia.repositorio.ProcessoImobiliarioRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.Optional;

/** A modalidade imobiliária no motor: basta ser um bean que implementa {@link Modalidade}. */
@ApplicationScoped
public class ModalidadeImobiliaria implements Modalidade<ProcessoImobiliario> {

    /** Acima disso o processamento recusa: o valor exige análise manual. */
    public static final BigDecimal LIMITE_AUTOMATICO = new BigDecimal("1000000.00");

    private final ProcessoImobiliarioRepository repositorio;

    public ModalidadeImobiliaria(ProcessoImobiliarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Class<ProcessoImobiliario> tipo() {
        return ProcessoImobiliario.class;
    }

    @Override
    public String prefixo() {
        return "IMO";
    }

    /** Por último a regra que vai ao banco: só consulta quando o pedido já passou nas outras. */
    @Override
    public Especificacao<ProcessoImobiliario> regras() {
        return RegrasImobiliarias.TODAS.e(this::matriculaSemProcessoEmAndamento);
    }

    @Override
    public Avaliacao avaliar(ProcessoImobiliario processo) {
        return processo.getValor().compareTo(LIMITE_AUTOMATICO) > 0
                ? Avaliacao.recusado("Valor acima de " + LIMITE_AUTOMATICO.toPlainString() + " exige análise manual")
                : Avaliacao.aprovado();
    }

    private Optional<String> matriculaSemProcessoEmAndamento(ProcessoImobiliario p) {
        return repositorio.existeEmAndamentoParaMatricula(p.getMatricula(), p.getUf())
                ? Optional.of("Já existe processo em andamento para a matrícula " + p.getMatricula() + "/" + p.getUf())
                : Optional.empty();
    }
}
```

## 6. Negócio: o motor

Fluxo único, idempotência, transações curtas (QuarkusTransaction), compare-and-set e efeito externo depois do commit.

### `src/main/java/com/example/processo/negocio/processo/ProcessoService.java`

```java
package com.example.processo.negocio.processo;

import com.example.processo.integracao.cadastro.CadastroIntegracao;
import com.example.processo.integracao.pdf.PdfIntegracao;
import com.example.processo.modelo.Processo;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.Situacao;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O motor: o mesmo fluxo de criação e o mesmo ciclo de vida para todas as modalidades. O que varia (regras, prefixo,
 * decisão do processamento) vem da {@link Modalidade}; os estados e as transições são da {@code Situacao}. É por aqui
 * que toda entrada (REST, fila) cria, processa ou cancela um processo.
 *
 * <pre>
 * criar:     regras comuns → regras da modalidade → situação cadastral (externo)  | fora de transação
 *            (com chave de idempotência já usada: devolve o processo existente, antes de tudo)
 *            → número → abrir → grava (+ chave)                                    | transação curta
 *            → pede o PDF de abertura (externo; falha só vai para o log)          | depois do commit
 * processar: carregar → ABERTO → EM_PROCESSAMENTO                                 | transação curta
 *            → modalidade avalia                                                  | fora de transação
 *            → CONCLUIDO ou RECUSADO                                              | transação curta
 * cancelar:  carregar → ABERTO → CANCELADO                                        | transação curta
 * </pre>
 *
 * As transações são curtas e explícitas ({@link QuarkusTransaction}): regra e chamada a sistema externo nunca ficam
 * dentro delas. As trocas de situação são compare-and-set: se outra requisição mudou o processo no meio, falha com
 * {@link ConflitoDeSituacaoException} em vez de sobrescrever.
 */
@ApplicationScoped
public class ProcessoService {

    private static final Logger LOG = Logger.getLogger(ProcessoService.class);

    private final List<Modalidade<?>> modalidades;
    private final ProcessoRepository repositorio;
    private final CadastroIntegracao cadastro;
    private final PdfIntegracao pdf;
    private final Clock relogio;

    ProcessoService(@Any Instance<Modalidade<?>> modalidades, ProcessoRepository repositorio,
                    CadastroIntegracao cadastro, PdfIntegracao pdf, Clock relogio) {
        this.modalidades = modalidades.stream().toList();
        this.repositorio = repositorio;
        this.cadastro = cadastro;
        this.pdf = pdf;
        this.relogio = relogio;
    }

    public <P extends Processo> P criar(P processo) {
        return criar(processo, null);
    }

    /**
     * Cria de forma idempotente: com uma {@code chave} já usada (ex.: o id de uma mensagem reentregue), devolve o
     * processo criado da primeira vez, sem reaplicar regras, gerar número nem pedir PDF. Duas criações simultâneas
     * com a mesma chave: a segunda falha ao gravar a chave, e quem tentar de novo cai neste caminho.
     */
    public <P extends Processo> P criar(P processo, String chave) {
        var modalidade = modalidadeDe(processo);
        if (chave != null) {
            var existente = repositorio.processoDaChave(chave).flatMap(repositorio::buscar);
            if (existente.isPresent()) {
                return modalidade.tipo().cast(existente.get());
            }
        }
        // A chamada ao sistema externo vem por último: só acontece se o pedido passou em todas as outras regras
        Especificacao<P> comuns = RegrasComuns.TODAS::violacao;
        comuns.e(modalidade.regras()).e(this::cadastroRegular).exigir(processo);

        QuarkusTransaction.requiringNew().run(() -> {
            processo.abrir(numero(modalidade), LocalDate.now(relogio));
            repositorio.inserir(processo);
            if (chave != null) {
                repositorio.registrarChave(chave, processo.getId());
            }
        });

        // Depois do commit: processo que não foi gravado não é impresso, e falha no PDF não desfaz o processo
        try {
            pdf.gerarAbertura(processo);
        } catch (SistemaExternoIndisponivelException e) {
            LOG.errorf(e, "Falha ao pedir o PDF de abertura do processo %s", processo.getNumero());
        }
        return processo;
    }

    public Processo processar(UUID id) {
        var processo = carregar(id);
        var de = processo.getSituacao();
        processo.iniciarProcessamento();
        gravarSituacao(processo, de);

        var avaliacao = avaliar(modalidadeDe(processo), processo);
        de = processo.getSituacao();
        if (avaliacao.aprovada()) {
            processo.concluir();
        } else {
            processo.recusar(avaliacao.motivo());
        }
        gravarSituacao(processo, de);
        return processo;
    }

    public Processo cancelar(UUID id) {
        var processo = carregar(id);
        var de = processo.getSituacao();
        processo.cancelar();
        gravarSituacao(processo, de);
        return processo;
    }

    /** Grava a nova situação só se o processo ainda estiver na anterior. */
    private void gravarSituacao(Processo processo, Situacao de) {
        boolean mudou = QuarkusTransaction.requiringNew().call(() -> repositorio.mudarSituacao(
                processo.getId(), de, processo.getSituacao(), processo.getMotivoRecusa()));
        if (!mudou) {
            throw new ConflitoDeSituacaoException(processo.getNumero());
        }
    }

    private Processo carregar(UUID id) {
        return repositorio.buscar(id).orElseThrow(() -> new ProcessoNaoEncontradoException(id));
    }

    private static <P extends Processo> Avaliacao avaliar(Modalidade<P> modalidade, Processo processo) {
        return modalidade.avaliar(modalidade.tipo().cast(processo));
    }

    private Optional<String> cadastroRegular(Processo processo) {
        return cadastro.regular(processo.getProponenteCpf())
                ? Optional.empty()
                : Optional.of("Situação cadastral irregular para o CPF " + processo.getProponenteCpf());
    }

    @SuppressWarnings("unchecked") // a modalidade foi escolhida por tipo().isInstance(processo)
    private <P extends Processo> Modalidade<P> modalidadeDe(P processo) {
        if (processo == null) {
            throw new IllegalArgumentException("Pedido sem processo");
        }
        var encontradas = modalidades.stream().filter(m -> m.tipo().isInstance(processo)).toList();
        if (encontradas.size() != 1) {
            throw new IllegalStateException(encontradas.size() + " modalidades registradas para "
                    + processo.getClass().getName() + "; deveria ser 1");
        }
        return (Modalidade<P>) encontradas.getFirst();
    }

    /** IMO-2026-000042: prefixo da modalidade, ano da abertura e o sequencial único. */
    private String numero(Modalidade<?> modalidade) {
        return "%s-%d-%06d".formatted(modalidade.prefixo(), LocalDate.now(relogio).getYear(),
                repositorio.proximoNumero());
    }
}
```

### `src/main/java/com/example/processo/negocio/processo/ConflitoDeSituacaoException.java`

```java
package com.example.processo.negocio.processo;

/** Outra requisição mudou a situação do processo entre a leitura e a gravação. */
public class ConflitoDeSituacaoException extends RuntimeException {

    public ConflitoDeSituacaoException(String numero) {
        super("Processo " + numero + " foi alterado por outra requisição; tente de novo");
    }
}
```

### `src/main/java/com/example/processo/negocio/processo/Relogio.java`

```java
package com.example.processo.negocio.processo;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/** O negócio recebe o relógio em vez de chamar LocalDate.now(): testes podem fixar a data. */
public class Relogio {

    @Produces
    @Singleton
    Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
```

## 7. Negócio: CRUD pelo caminho curto

Entidade válida por construção, serviço com @Transactional, repository.

### `src/main/java/com/example/processo/modelo/Agencia.java`

```java
package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

/**
 * Agência: cadastro simples, sem fluxo nem estados. Válida por construção: inválida dá 422 com o motivo, sem uma
 * camada de regras à parte. O construtor sem argumentos é só do Hibernate (lê o que já foi gravado válido).
 */
@Entity
@Table(name = "AGENCIA")
public class Agencia {

    @Id
    @Column(length = 4)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 2)
    private String uf;

    /** NUMBER(1) com 0/1: funciona em qualquer Oracle (BOOLEAN nativo só existe a partir do 23). */
    @Convert(converter = NumericBooleanConverter.class)
    @Column(nullable = false)
    private boolean ativa;

    protected Agencia() {
    }

    public Agencia(String codigo, String nome, String uf, boolean ativa) {
        if (codigo == null || !codigo.matches("\\d{4}")) {
            throw new RegraVioladaException("Código da agência deve ter 4 dígitos: " + codigo);
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("Nome da agência é obrigatório");
        }
        if (uf == null || !uf.matches("[A-Z]{2}")) {
            throw new RegraVioladaException("UF inválida: " + uf);
        }
        this.codigo = codigo;
        this.nome = nome;
        this.uf = uf;
        this.ativa = ativa;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getUf() { return uf; }
    public boolean isAtiva() { return ativa; }
}
```

### `src/main/java/com/example/processo/negocio/agencia/AgenciaService.java`

```java
package com.example.processo.negocio.agencia;

import com.example.processo.modelo.Agencia;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.persistencia.repositorio.AgenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Cadastro de agências: o caminho curto para o que é só CRUD (ADR 0012). Um serviço com {@code @Transactional} sobre o
 * repository, sem motor nem estados. Se surgir fluxo ou variação por tipo, aí sim vira uma modalidade do processo.
 */
@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository repositorio;

    AgenciaService(AgenciaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Agencia> todas() {
        return repositorio.todas();
    }

    public Agencia porCodigo(String codigo) {
        return repositorio.buscar(codigo).orElseThrow(() -> new AgenciaNaoEncontradaException(codigo));
    }

    @Transactional
    public Agencia criar(Agencia agencia) {
        if (repositorio.buscar(agencia.getCodigo()).isPresent()) {
            throw new RegraVioladaException("Já existe agência com o código " + agencia.getCodigo());
        }
        repositorio.inserir(agencia);
        return agencia;
    }

    @Transactional
    public Agencia alterar(Agencia agencia) {
        porCodigo(agencia.getCodigo());
        repositorio.atualizar(agencia);
        return agencia;
    }

    @Transactional
    public void excluir(String codigo) {
        porCodigo(codigo);
        repositorio.excluir(codigo);
    }
}
```

### `src/main/java/com/example/processo/negocio/agencia/AgenciaNaoEncontradaException.java`

```java
package com.example.processo.negocio.agencia;

public class AgenciaNaoEncontradaException extends RuntimeException {

    public AgenciaNaoEncontradaException(String codigo) {
        super("Agência " + codigo + " não encontrada");
    }
}
```

## 8. Persistência: repositories

Insert/find polimórficos, sequence, compare-and-set em SQL nativo, entidade técnica escondida (idempotência), consulta própria de um tipo e CRUD.

### `src/main/java/com/example/processo/persistencia/repositorio/ProcessoRepository.java`

```java
package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.Processo;
import com.example.processo.modelo.Situacao;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

import java.util.Optional;
import java.util.UUID;

/**
 * Gravação e leitura de processos de qualquer modalidade. A implementação é gerada pelo Hibernate Processor na
 * compilação ({@code ProcessoRepository_}). Como {@link Processo} é a raiz da herança, gravar uma filha insere nas duas
 * tabelas e ler pelo id devolve a filha certa ({@code ProcessoImobiliario}, {@code ProcessoVeicular}...).
 */
@Repository
public interface ProcessoRepository {

    @Insert
    void inserir(Processo processo);

    @Find
    Optional<Processo> buscar(UUID id);

    /** Sequence do Oracle: única para todas as modalidades e sem corrida entre transações. */
    @SQL("select SEQ_NUMERO_PROCESSO.nextval from dual")
    long proximoNumero();

    /**
     * SQL nativo de propósito: a situação está só na tabela pai, e um update HQL numa hierarquia JOINED faz o
     * Hibernate usar a estratégia multi-tabela (tabelas temporárias no Oracle).
     */
    @SQL("update PROCESSO set SITUACAO = :para, MOTIVO_RECUSA = :motivo where ID = :id and SITUACAO = :de")
    int atualizarSituacao(UUID id, String de, String para, String motivo);

    /**
     * Compare-and-set: troca a situação só se ela ainda for {@code de}. {@code false} quer dizer que outra requisição
     * mudou o processo antes.
     */
    default boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
        return atualizarSituacao(id, de.name(), para.name(), motivoRecusa) == 1;
    }

    @Find
    Optional<ChaveIdempotencia> buscarChave(String chave);

    @Insert
    void inserirChave(ChaveIdempotencia chave);

    /** O processo já criado com esta chave de idempotência, se houver. */
    default Optional<UUID> processoDaChave(String chave) {
        return buscarChave(chave).map(ChaveIdempotencia::getProcessoId);
    }

    /** Falha se a chave já existir (PK): duas criações simultâneas com a mesma chave não passam as duas. */
    default void registrarChave(String chave, UUID processoId) {
        inserirChave(new ChaveIdempotencia(chave, processoId));
    }
}
```

### `src/main/java/com/example/processo/persistencia/repositorio/ChaveIdempotencia.java`

```java
package com.example.processo.persistencia.repositorio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Liga a chave de idempotência de quem pediu a criação (ex.: id da mensagem) ao processo criado. Tabela técnica, não
 * vocabulário do negócio: só o {@link ProcessoRepository} a conhece.
 */
@Entity
@Table(name = "PROCESSO_IDEMPOTENCIA")
class ChaveIdempotencia {

    @Id
    @Column(length = 100)
    private String chave;

    @Column(name = "PROCESSO_ID", nullable = false)
    private UUID processoId;

    protected ChaveIdempotencia() {
    }

    ChaveIdempotencia(String chave, UUID processoId) {
        this.chave = chave;
        this.processoId = processoId;
    }

    String getChave() { return chave; }
    UUID getProcessoId() { return processoId; }
}
```

### `src/main/java/com/example/processo/persistencia/repositorio/ProcessoImobiliarioRepository.java`

```java
package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.Situacao;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

/** Consultas que só a modalidade imobiliária usa. Modalidade sem consulta própria não precisa de repository. */
@Repository
public interface ProcessoImobiliarioRepository {

    List<Situacao> EM_ANDAMENTO = List.of(Situacao.ABERTO, Situacao.EM_PROCESSAMENTO);

    /** situacao vem da entidade pai: a consulta na filha enxerga os campos herdados (join automático). */
    @Query("select count(*) from ProcessoImobiliario where matricula = :matricula and uf = :uf and situacao in :situacoes")
    long contar(String matricula, String uf, List<Situacao> situacoes);

    /** Em andamento = aberto ou em processamento. */
    default boolean existeEmAndamentoParaMatricula(String matricula, String uf) {
        return contar(matricula, uf, EM_ANDAMENTO) > 0;
    }
}
```

### `src/main/java/com/example/processo/persistencia/repositorio/AgenciaRepository.java`

```java
package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.Agencia;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenciaRepository {

    @Find
    Optional<Agencia> buscar(String codigo);

    @Query("from Agencia order by codigo")
    List<Agencia> todas();

    @Insert
    void inserir(Agencia agencia);

    @Update
    void atualizar(Agencia agencia);

    @Delete
    void excluir(String codigo);
}
```

## 9. Persistência: consultas para as telas

Projeções planas com select new e paginação; a apresentação usa direto.

### `src/main/java/com/example/processo/persistencia/consulta/ProcessoConsultas.java`

```java
package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Consultas somente leitura para as telas: projetam direto das tabelas para o formato da resposta, sem carregar a
 * entidade nem passar pelas regras. A apresentação usa este repository direto, sem um serviço que só repassaria.
 */
@Repository
public interface ProcessoConsultas {

    String RESUMO = """
            select new com.example.processo.persistencia.consulta.ResumoProcesso(
                p.id, p.numero, p.segmento, p.proponenteNome, p.valor, p.situacao, p.dataAbertura)
            from Processo p""";

    /**
     * Listagem polimórfica e paginada: consulta o pai (todas as modalidades) projetando só colunas de PROCESSO, então
     * modalidade nova não acrescenta join aqui. O Hibernate gera também o count para o total.
     */
    @Query(RESUMO + " order by p.numero")
    Page<ResumoProcesso> todos(PageRequest pagina);

    @Query(RESUMO + " where p.situacao = :situacao order by p.numero")
    Page<ResumoProcesso> naSituacao(Situacao situacao, PageRequest pagina);

    /** Detalhe de uma modalidade: consulta direto a filha, que já enxerga os campos herdados do pai. */
    @Query("""
            select new com.example.processo.persistencia.consulta.DetalheImobiliario(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.matricula, p.tipoImovel, p.valorAvaliacao, p.uf)
            from ProcessoImobiliario p
            where p.id = :id""")
    Optional<DetalheImobiliario> imobiliario(UUID id);

    @Query("""
            select new com.example.processo.persistencia.consulta.DetalheVeicular(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.placa, p.anoFabricacao, p.valorFipe)
            from ProcessoVeicular p
            where p.id = :id""")
    Optional<DetalheVeicular> veicular(UUID id);
}
```

### `src/main/java/com/example/processo/persistencia/consulta/ResumoProcesso.java`

```java
package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Linha da listagem: só os campos do pai, iguais para todas as modalidades. */
public record ResumoProcesso(UUID id, String numero, String segmento, String proponente, BigDecimal valor,
                             Situacao situacao, LocalDate dataAbertura) {
}
```

### `src/main/java/com/example/processo/persistencia/consulta/DetalheVeicular.java`

```java
package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Processo veicular completo: campos do pai e da filha. Plano e top-level, como o {@link DetalheImobiliario}. */
public record DetalheVeicular(UUID id, String numero, String proponenteNome, String proponenteCpf,
                              BigDecimal valor, Situacao situacao, String motivoRecusa,
                              LocalDate dataAbertura, String placa, Integer anoFabricacao, BigDecimal valorFipe) {
}
```

## 10. Integração com sistema externo

Cliente, simulação do perfil dev, formato externo e a classe que o negócio chama.

### `src/main/java/com/example/processo/integracao/cadastro/CadastroClient.java`

```java
package com.example.processo.integracao.cadastro;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente do sistema externo de cadastro; o endereço vem de quarkus.rest-client.cadastro.url. Fora do perfil dev
 * é sempre esta chamada HTTP; no dev, o {@link CadastroClientSimulado} toma o lugar dela.
 */
@RegisterRestClient(configKey = "cadastro")
@Path("/situacoes")
public interface CadastroClient {

    @GET
    @Path("{documento}")
    RespostaCadastro consultar(@PathParam("documento") String documento);
}
```

### `src/main/java/com/example/processo/integracao/cadastro/CadastroClientSimulado.java`

```java
package com.example.processo.integracao.cadastro;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Simulação do sistema de cadastro para o perfil dev, para rodar a aplicação sem o sistema externo. Responde
 * sempre REGULAR, sem rede.
 *
 * <ul>
 *   <li>{@code @IfBuildProfile("dev")}: o bean só existe no build de dev. No jar de produção (e nos ITs, que
 *       rodam contra ele) não há simulação: vale o REST Client.</li>
 *   <li>{@code @RestClient} + {@code @Alternative @Priority}: mesmo tipo e qualificador do REST Client gerado
 *       pelo Quarkus, com prioridade sobre ele. Quem injeta ({@link CadastroIntegracao}) não sabe qual
 *       recebeu.</li>
 * </ul>
 */
@IfBuildProfile("dev")
@Alternative
@Priority(1)
@RestClient
@ApplicationScoped
public class CadastroClientSimulado implements CadastroClient {

    @Override
    public RespostaCadastro consultar(String documento) {
        return new RespostaCadastro(documento, RespostaCadastro.REGULAR);
    }
}
```

### `src/main/java/com/example/processo/integracao/cadastro/RespostaCadastro.java`

```java
package com.example.processo.integracao.cadastro;

/** Modelo do sistema de cadastro, como ele responde. Não sai do pacote cadastro: a integração só usa {@link #regular()}. */
public record RespostaCadastro(String documento, String situacao) {

    public static final String REGULAR = "REGULAR";

    /** Só a situação REGULAR conta como regular; qualquer outra (ou nenhuma) não. */
    public boolean regular() {
        return REGULAR.equals(situacao);
    }
}
```

### `src/main/java/com/example/processo/integracao/cadastro/CadastroIntegracao.java`

```java
package com.example.processo.integracao.cadastro;

import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * O que o negócio chama para falar com o sistema de cadastro: chama o cliente HTTP e traduz a resposta e os erros. O
 * formato do sistema externo ({@link RespostaCadastro}) e as exceções do REST Client não passam daqui.
 */
@ApplicationScoped
public class CadastroIntegracao {

    private final CadastroClient cliente;

    public CadastroIntegracao(@RestClient CadastroClient cliente) {
        this.cliente = cliente;
    }

    /** Erro HTTP (WebApplicationException) ou de rede/timeout (ProcessingException) vira indisponibilidade. */
    public boolean regular(String cpf) {
        try {
            return cliente.consultar(cpf).regular();
        } catch (WebApplicationException | ProcessingException e) {
            throw new SistemaExternoIndisponivelException("de cadastro", e);
        }
    }
}
```

## 11. Apresentação: REST

Resource que só traduz, request com paraModelo(), response com de(...), leitura direta das consultas, paginação com 400 e Problem Details.

### `src/main/java/com/example/processo/apresentacao/rest/ProcessoVeicularResource.java`

```java
package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.NovoProcessoVeicularRequest;
import com.example.processo.apresentacao.rest.dto.ProcessoCriadoResponse;
import com.example.processo.negocio.processo.ProcessoService;
import com.example.processo.persistencia.consulta.DetalheVeicular;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Endpoints da modalidade veicular: mesmo formato do imobiliário, mesmo motor. */
@Path(ProcessoVeicularResource.CAMINHO)
public class ProcessoVeicularResource {

    static final String CAMINHO = "/processos/veiculares";

    private final ProcessoService motor;
    private final ProcessoConsultas consultas;

    ProcessoVeicularResource(ProcessoService motor, ProcessoConsultas consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoVeicularRequest request) {
        var criado = ProcessoCriadoResponse.de(motor.criar(request.paraModelo()));
        return Response.created(URI.create(CAMINHO + "/" + criado.id())).entity(criado).build();
    }

    @GET
    @Path("{id}")
    public DetalheVeicular detalhe(@PathParam("id") UUID id) {
        return consultas.veicular(id).orElseThrow(NotFoundException::new);
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/ProcessosResource.java`

```java
package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.PaginaResponse;
import com.example.processo.apresentacao.rest.dto.SituacaoResponse;
import com.example.processo.modelo.Situacao;
import com.example.processo.negocio.processo.ProcessoService;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import com.example.processo.persistencia.consulta.ResumoProcesso;
import jakarta.data.page.PageRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.UUID;

/**
 * O que vale para todas as modalidades: a listagem e o ciclo de vida (processar, cancelar), que é do motor. A
 * criação e o detalhe são por modalidade, no resource de cada uma, porque o corpo é da modalidade.
 */
@Path("/processos")
public class ProcessosResource {

    static final int TAMANHO_MAXIMO = 100;

    private final ProcessoConsultas consultas;
    private final ProcessoService motor;

    ProcessosResource(ProcessoConsultas consultas, ProcessoService motor) {
        this.consultas = consultas;
        this.motor = motor;
    }

    /**
     * {@code ?situacao=ABERTO} filtra; valor fora do enum responde 404, como manda o JAX-RS para @QueryParam.
     * {@code ?pagina=1&tamanho=20} pagina (padrões); fora de {@code pagina >= 1} e {@code 1..100} responde 400.
     */
    @GET
    public PaginaResponse<ResumoProcesso> listar(@QueryParam("situacao") Situacao situacao,
                                                 @QueryParam("pagina") @DefaultValue("1") int pagina,
                                                 @QueryParam("tamanho") @DefaultValue("20") int tamanho) {
        if (pagina < 1 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new BadRequestException("pagina deve ser >= 1 e tamanho entre 1 e " + TAMANHO_MAXIMO);
        }
        var pedido = PageRequest.ofPage(pagina, tamanho, true);
        return PaginaResponse.de(situacao == null ? consultas.todos(pedido) : consultas.naSituacao(situacao, pedido));
    }

    @POST
    @Path("{id}/processamento")
    public SituacaoResponse processar(@PathParam("id") UUID id) {
        return SituacaoResponse.de(motor.processar(id));
    }

    @POST
    @Path("{id}/cancelamento")
    public void cancelar(@PathParam("id") UUID id) {
        motor.cancelar(id);
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/AgenciasResource.java`

```java
package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.AgenciaResponse;
import com.example.processo.apresentacao.rest.dto.AlteracaoAgenciaRequest;
import com.example.processo.apresentacao.rest.dto.NovaAgenciaRequest;
import com.example.processo.negocio.agencia.AgenciaService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

/**
 * CRUD de agências: o resource chama o serviço do cadastro e devolve a resposta da API. Compare com os resources de
 * processo, que passam pelo motor: aqui não há fluxo, então não há motor.
 */
@Path("/agencias")
public class AgenciasResource {

    private final AgenciaService agencias;

    AgenciasResource(AgenciaService agencias) {
        this.agencias = agencias;
    }

    @GET
    public List<AgenciaResponse> listar() {
        return agencias.todas().stream().map(AgenciaResponse::de).toList();
    }

    @GET
    @Path("{codigo}")
    public AgenciaResponse porCodigo(@PathParam("codigo") String codigo) {
        return AgenciaResponse.de(agencias.porCodigo(codigo));
    }

    @POST
    public Response criar(NovaAgenciaRequest request) {
        var criada = AgenciaResponse.de(agencias.criar(request.paraModelo()));
        return Response.created(URI.create("/agencias/" + criada.codigo())).entity(criada).build();
    }

    @PUT
    @Path("{codigo}")
    public AgenciaResponse alterar(@PathParam("codigo") String codigo, AlteracaoAgenciaRequest request) {
        return AgenciaResponse.de(agencias.alterar(request.paraModelo(codigo)));
    }

    @DELETE
    @Path("{codigo}")
    public void excluir(@PathParam("codigo") String codigo) {
        agencias.excluir(codigo);
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/TratadorDeErros.java`

```java
package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.Problema;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.TransicaoInvalidaException;
import com.example.processo.negocio.agencia.AgenciaNaoEncontradaException;
import com.example.processo.negocio.processo.ConflitoDeSituacaoException;
import com.example.processo.negocio.processo.ProcessoNaoEncontradoException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Exceções → Problem Details (RFC 9457), para todos os endpoints. Cada tipo de problema tem um
 * {@code type} fixo: é por ele, e não pelo texto, que um cliente decide o que fazer.
 */
public class TratadorDeErros {

    @ServerExceptionMapper
    public Response regraViolada(RegraVioladaException e) {
        return problema(422, "regra-violada", "Regra de negócio violada", e.getMessage());
    }

    @ServerExceptionMapper
    public Response transicaoInvalida(TransicaoInvalidaException e) {
        return problema(422, "transicao-invalida", "Transição de situação inválida", e.getMessage());
    }

    @ServerExceptionMapper
    public Response naoEncontrado(ProcessoNaoEncontradoException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", e.getMessage());
    }

    @ServerExceptionMapper
    public Response naoEncontrado(AgenciaNaoEncontradaException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", e.getMessage());
    }

    /** Detalhe por id inexistente, caminho desconhecido ou enum inválido em @QueryParam: o mesmo formato. */
    @ServerExceptionMapper
    public Response naoEncontrado(NotFoundException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", "Recurso não encontrado");
    }

    @ServerExceptionMapper
    public Response conflito(ConflitoDeSituacaoException e) {
        return problema(409, "conflito", "Conflito de concorrência", e.getMessage());
    }

    @ServerExceptionMapper
    public Response parametroInvalido(BadRequestException e) {
        return problema(400, "parametro-invalido", "Parâmetro inválido", e.getMessage());
    }

    @ServerExceptionMapper
    public Response indisponivel(SistemaExternoIndisponivelException e) {
        return problema(503, "sistema-indisponivel", "Sistema externo indisponível", e.getMessage());
    }

    private static Response problema(int status, String tipo, String titulo, String detalhe) {
        return Response.status(status)
                .type(Problema.MEDIA_TYPE)
                .entity(new Problema("/problemas/" + tipo, titulo, status, detalhe))
                .build();
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/NovoProcessoVeicularRequest.java`

```java
package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.Valor;

import java.math.BigDecimal;

/** Corpo do POST /processos/veiculares. */
public record NovoProcessoVeicularRequest(ProponenteRequest proponente, BigDecimal valor, VeiculoRequest veiculo) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public ProcessoVeicular paraModelo() {
        var p = proponente == null ? new ProponenteRequest(null, null) : proponente;
        var v = veiculo == null ? new VeiculoRequest(null, null, null) : veiculo;
        return new ProcessoVeicular(p.nome(), p.paraCpf(), valor == null ? null : new Valor(valor),
                v.placa(), v.anoFabricacao(), v.valorFipe());
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/ProponenteRequest.java`

```java
package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Cpf;

/** CPF só com os 11 dígitos. */
public record ProponenteRequest(String nome, String cpf) {

    /**
     * Sem proponente no corpo, o CPF segue nulo: quem recusa é a regra comum do nome, com a mensagem dela. CPF inválido
     * já para aqui, ao virar {@code Cpf} (422 com o motivo).
     */
    Cpf paraCpf() {
        return nome == null && cpf == null ? null : new Cpf(cpf);
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/ProcessoCriadoResponse.java`

```java
package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Processo;

import java.util.UUID;

/** Corpo do 201 de qualquer modalidade: o id e o número gerado. */
public record ProcessoCriadoResponse(UUID id, String numero) {

    public static ProcessoCriadoResponse de(Processo processo) {
        return new ProcessoCriadoResponse(processo.getId(), processo.getNumero());
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/SituacaoResponse.java`

```java
package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Processo;
import com.example.processo.modelo.Situacao;

/** Resultado de processar: a nova situação e, se recusado, o motivo. */
public record SituacaoResponse(Situacao situacao, String motivo) {

    public static SituacaoResponse de(Processo processo) {
        return new SituacaoResponse(processo.getSituacao(), processo.getMotivoRecusa());
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/AgenciaResponse.java`

```java
package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Agencia;

/** Agência como a API devolve: a entidade não sai na resposta, para o schema poder mudar sem mudar o contrato. */
public record AgenciaResponse(String codigo, String nome, String uf, boolean ativa) {

    public static AgenciaResponse de(Agencia agencia) {
        return new AgenciaResponse(agencia.getCodigo(), agencia.getNome(), agencia.getUf(), agencia.isAtiva());
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/PaginaResponse.java`

```java
package com.example.processo.apresentacao.rest.dto;

import jakarta.data.page.Page;

import java.util.List;

/** Uma página da listagem: os itens, onde se está e quantos existem no total. {@code pagina} começa em 1. */
public record PaginaResponse<T>(List<T> itens, long pagina, int tamanho, long total) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.content(), page.pageRequest().page(), page.pageRequest().size(),
                page.totalElements());
    }
}
```

### `src/main/java/com/example/processo/apresentacao/rest/dto/Problema.java`

```java
package com.example.processo.apresentacao.rest.dto;

/**
 * Corpo de toda resposta de erro, no formato Problem Details (RFC 9457), servido como
 * {@code application/problem+json}. {@code type} identifica o tipo de problema (estável, para o cliente decidir);
 * {@code title} o resume; {@code detail} é o motivo desta ocorrência, apresentável ao usuário.
 */
public record Problema(String type, String title, int status, String detail) {

    public static final String MEDIA_TYPE = "application/problem+json";
}
```

## 12. Apresentação: fila

Consumidor AMQP com ack/nack por tipo de erro, idempotência pelo id da mensagem e payload em qualquer formato.

### `src/main/java/com/example/processo/apresentacao/mensageria/NovosProcessosImobiliarios.java`

```java
package com.example.processo.apresentacao.mensageria;

import com.example.processo.apresentacao.mensageria.dto.NovoProcessoImobiliarioMensagem;
import com.example.processo.apresentacao.mensageria.dto.RespostaNovoProcesso;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.negocio.processo.ProcessoService;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Cria processo imobiliário a partir da fila: a mesma entrada do POST /processos/imobiliarios, por outro canal.
 * Traduz a mensagem, chama o motor e decide o destino da mensagem pelo tipo do erro:
 *
 * <ul>
 *   <li>criado, ou já criado antes com o mesmo id: responde {@code criado} e confirma (ack);</li>
 *   <li>regra violada ou mensagem malformada: responde {@code recusado} e confirma; reentregar não mudaria o
 *       resultado;</li>
 *   <li>qualquer outra falha (sistema externo fora do ar, banco): devolve ao broker (nack) para reentrega. O id da
 *       mensagem é a chave de idempotência, então a reentrega não duplica o processo.</li>
 * </ul>
 *
 * Não abre transação: o motor já usa as dele, curtas.
 */
@ApplicationScoped
public class NovosProcessosImobiliarios {

    private static final Logger LOG = Logger.getLogger(NovosProcessosImobiliarios.class);

    private final ProcessoService motor;
    private final Emitter<RespostaNovoProcesso> respostas;

    NovosProcessosImobiliarios(ProcessoService motor,
                               @Channel("processos-imobiliarios-respostas") Emitter<RespostaNovoProcesso> respostas) {
        this.motor = motor;
        this.respostas = respostas;
    }

    /**
     * {@code @ActivateRequestContext}: fora do HTTP não há request context, e as leituras do motor fora de transação
     * (chave de idempotência, carregar o processo) usam a StatelessSession, que precisa de um. Todo canal de entrada
     * que não seja REST (fila, agendamento) precisa disso.
     */
    @Incoming("processos-imobiliarios-novos")
    @Blocking
    @ActivateRequestContext
    public CompletionStage<Void> consumir(Message<Object> mensagem) {
        NovoProcessoImobiliarioMensagem pedido;
        try {
            pedido = json(mensagem.getPayload()).mapTo(NovoProcessoImobiliarioMensagem.class);
        } catch (RuntimeException e) {
            LOG.warnf(e, "Mensagem malformada descartada: %s", mensagem.getPayload());
            respostas.send(RespostaNovoProcesso.recusado(null, "Mensagem malformada"));
            return mensagem.ack();
        }
        if (pedido.id() == null || pedido.id().isBlank()) {
            respostas.send(RespostaNovoProcesso.recusado(null, "Mensagem sem id"));
            return mensagem.ack();
        }

        try {
            var processo = motor.criar(pedido.paraModelo(), pedido.id());
            respostas.send(RespostaNovoProcesso.criado(pedido.id(), processo));
            return mensagem.ack();
        } catch (RegraVioladaException e) {
            respostas.send(RespostaNovoProcesso.recusado(pedido.id(), e.getMessage()));
            return mensagem.ack();
        } catch (RuntimeException e) {
            LOG.warnf(e, "Mensagem %s devolvida para reentrega", pedido.id());
            return mensagem.nack(e);
        }
    }

    /**
     * O connector entrega JSON como {@code JsonObject} quando a mensagem vem com content-type application/json, e como
     * texto ou bytes quando não vem (o comum num produtor de mainframe). Os três formatos são aceitos.
     */
    static JsonObject json(Object payload) {
        return switch (payload) {
            case JsonObject json -> json;
            case String texto -> new JsonObject(texto);
            case Buffer buffer -> buffer.toJsonObject();
            case byte[] bytes -> Buffer.buffer(bytes).toJsonObject();
            default -> throw new IllegalArgumentException("Payload não suportado: " + payload.getClass().getName());
        };
    }
}
```

### `src/main/java/com/example/processo/apresentacao/mensageria/dto/NovoProcessoImobiliarioMensagem.java`

```java
package com.example.processo.apresentacao.mensageria.dto;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;

import java.math.BigDecimal;

/**
 * Mensagem da fila processos.imobiliarios.novos. Layout próprio do canal, plano como um registro do mainframe; não é
 * o corpo do REST. {@code id} identifica a mensagem e é a chave de idempotência: reentregar não cria outro processo.
 */
public record NovoProcessoImobiliarioMensagem(String id, String nome, String cpf, BigDecimal valor, String matricula,
                                               TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf) {

    /** Campos ausentes seguem nulos para as regras recusarem; CPF e valor inválidos param ao virar value object. */
    public ProcessoImobiliario paraModelo() {
        return new ProcessoImobiliario(nome, new Cpf(cpf), valor == null ? null : new Valor(valor),
                matricula, tipoImovel, valorAvaliacao, uf);
    }
}
```

### `src/main/java/com/example/processo/apresentacao/mensageria/dto/RespostaNovoProcesso.java`

```java
package com.example.processo.apresentacao.mensageria.dto;

import com.example.processo.modelo.Processo;

import java.util.UUID;

/**
 * Resposta publicada em processos.imobiliarios.respostas para cada mensagem tratada: o processo criado ou o motivo da
 * recusa. {@code idMensagem} liga a resposta ao pedido.
 */
public record RespostaNovoProcesso(String idMensagem, boolean criado, UUID processoId, String numero, String motivo) {

    public static RespostaNovoProcesso criado(String idMensagem, Processo processo) {
        return new RespostaNovoProcesso(idMensagem, true, processo.getId(), processo.getNumero(), null);
    }

    public static RespostaNovoProcesso recusado(String idMensagem, String motivo) {
        return new RespostaNovoProcesso(idMensagem, false, null, null, motivo);
    }
}
```

## 13. package-info de uma camada

Cada camada diz o que contém e o que pode ou não usar.

### `src/main/java/com/example/processo/negocio/package-info.java`

```java
/**
 * Camada de negócio: a lógica do sistema. {@code processo} é o motor ({@code ProcessoService}), igual para todas as
 * modalidades; cada modalidade ({@code imobiliario}, {@code veicular}) tem um pacote com as regras e a decisão dela;
 * cadastros simples ({@code agencia}) têm um serviço CRUD.
 *
 * <ul>
 *   <li><b>Pode usar:</b> {@code integracao}, {@code persistencia} e {@code modelo}.</li>
 *   <li><b>Não pode:</b> {@code apresentacao}, nem uma modalidade usar a outra (só o motor, em {@code processo}).</li>
 *   <li><b>Transações:</b> curtas e explícitas no motor ({@code QuarkusTransaction}); {@code @Transactional} nos
 *       cadastros simples. Chamada a sistema externo nunca dentro de transação.</li>
 * </ul>
 */
package com.example.processo.negocio;
```

## 14. Migrations

Tabela filha de um tipo (PK = FK) e uma tabela de CRUD.

### `src/main/resources/db/migration/V3__processo_veicular.sql`

```sql
-- Filha do segmento veicular: mesma PK do pai (herança JOINED). Segmento novo = tabela filha nova.
CREATE TABLE PROCESSO_VEICULAR (
    PROCESSO_ID    RAW(16)       NOT NULL,
    PLACA          VARCHAR2(7)   NOT NULL,
    ANO_FABRICACAO NUMBER(4)     NOT NULL,
    VALOR_FIPE     NUMBER(15, 2) NOT NULL,
    CONSTRAINT PK_PROCESSO_VEICULAR PRIMARY KEY (PROCESSO_ID),
    CONSTRAINT FK_PROCESSO_VEICULAR FOREIGN KEY (PROCESSO_ID) REFERENCES PROCESSO (ID)
);
```

### `src/main/resources/db/migration/V5__agencia.sql`

```sql
-- Cadastro simples (CRUD), sem relação com o fluxo de processos: o exemplo do caminho curto (ADR 0012)
CREATE TABLE AGENCIA (
    CODIGO VARCHAR2(4)   NOT NULL,
    NOME   VARCHAR2(100) NOT NULL,
    UF     VARCHAR2(2)   NOT NULL,
    ATIVA  NUMBER(1)     NOT NULL,
    CONSTRAINT PK_AGENCIA PRIMARY KEY (CODIGO),
    CONSTRAINT CK_AGENCIA_ATIVA CHECK (ATIVA IN (0, 1))
);
```

## 15. Configuração

O POM completo (um módulo, hibernate-processor, JaCoCo com três fontes, failsafe) e o application.properties (sem URL de banco nem amqp-host).

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Um módulo só, organizado em camadas por pacote (apresentacao, negocio, integracao, persistencia, modelo).
         As regras entre as camadas ficam no ArquiteturaTest (ADR 0014). -->
    <groupId>com.example</groupId>
    <artifactId>processo</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
        <quarkus.platform.version>3.33.4</quarkus.platform.version>
        <compiler-plugin.version>3.14.0</compiler-plugin.version>
        <surefire-plugin.version>3.5.3</surefire-plugin.version>
        <archunit.version>1.5.1</archunit.version>
        <jacoco-plugin.version>0.8.15</jacoco-plugin.version>
        <!-- Preenchido pelo jacoco:prepare-agent; o valor vazio evita erro se o JaCoCo for pulado -->
        <argLine/>

        <!-- Sonar (rodado pelo CI num passo separado, com -DskipTests, depois do build que executa os testes) -->
        <!-- Sem isso o sonar-maven-plugin 3.6 não reconhece records ("Parse error at line 1 column 1") -->
        <sonar.java.source>${maven.compiler.release}</sonar.java.source>
        <!-- O Sonar lê só target/surefire-reports por padrão; os @QuarkusIntegrationTest ficam em failsafe-reports -->
        <sonar.junit.reportPaths>${project.build.directory}/surefire-reports,${project.build.directory}/failsafe-reports</sonar.junit.reportPaths>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- persistencia + modelo: entidades, repositories Jakarta Data e migrations -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-hibernate-orm</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.data</groupId>
            <artifactId>jakarta.data-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-oracle</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-flyway-oracle</artifactId>
        </dependency>
        <!-- apresentacao: REST e fila (AMQP 1.0; Artemis pelo Dev Services no dev e nos testes) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-messaging-amqp</artifactId>
        </dependency>
        <!-- integracao: clientes HTTP dos sistemas externos -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-client-jackson</artifactId>
        </dependency>
        <!-- Contrato da API em /q/openapi (Swagger UI em /q/swagger-ui no dev) e /q/health para o deploy -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-openapi</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>

        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- Cobertura dos @QuarkusTest: instrumenta as classes antes das transformações do Quarkus (o agente comum não
             reconhece classe transformada, como bean sem construtor vazio e entidade com enhancement) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jacoco</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- Troca os canais da fila por memória no teste do consumidor, sem broker -->
        <dependency>
            <groupId>io.smallrye.reactive</groupId>
            <artifactId>smallrye-reactive-messaging-in-memory</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit</artifactId>
            <version>${archunit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
                <configuration>
                    <parameters>true</parameters>
                    <!-- Hibernate Processor: gera o metamodel (Processo_) e as implementações dos repositories
                         Jakarta Data, validando o HQL de @Query na compilação -->
                    <annotationProcessorPathsUseDepMgmt>true</annotationProcessorPathsUseDepMgmt>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.hibernate.orm</groupId>
                            <artifactId>hibernate-processor</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${surefire-plugin.version}</version>
                <configuration>
                    <!-- @{argLine} (avaliado tarde) recebe o agente do JaCoCo -->
                    <argLine>@{argLine}</argLine>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
            <!-- @QuarkusIntegrationTest (*IT) roda contra o jar empacotado, na fase verify -->
            <plugin>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>${surefire-plugin.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                        <!-- O jar que os ITs sobem roda com o agente do JaCoCo: a API (resources, consumidor) entra na
                             cobertura sem teste duplicado -->
                        <quarkus.test.arg-line>${jacoco.agent.it}</quarkus.test.arg-line>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
            <!-- Cobertura em target/site/jacoco (jacoco.xml para o Sonar, index.html para pessoas), unindo em
                 target/jacoco-merged.exec os testes puros (jacoco.exec), os @QuarkusTest (jacoco-quarkus.exec, do
                 quarkus-jacoco) e os ITs (jacoco-it.exec, do agente no jar) -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco-plugin.version}</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                        <configuration>
                            <!-- O que roda no Quarkus dos @QuarkusTest fica com o quarkus-jacoco -->
                            <exclClassLoaders>*QuarkusClassLoader</exclClassLoaders>
                        </configuration>
                    </execution>
                    <!-- Só preenche a propriedade jacoco.agent.it com o -javaagent; quem a usa é o failsafe -->
                    <execution>
                        <id>prepare-agent-integration</id>
                        <goals>
                            <goal>prepare-agent-integration</goal>
                        </goals>
                        <configuration>
                            <propertyName>jacoco.agent.it</propertyName>
                        </configuration>
                    </execution>
                    <execution>
                        <id>merge</id>
                        <phase>post-integration-test</phase>
                        <goals>
                            <goal>merge</goal>
                        </goals>
                        <configuration>
                            <fileSets>
                                <fileSet>
                                    <directory>${project.build.directory}</directory>
                                    <includes>
                                        <include>jacoco.exec</include>
                                        <include>jacoco-quarkus.exec</include>
                                        <include>jacoco-it.exec</include>
                                    </includes>
                                </fileSet>
                            </fileSets>
                            <destFile>${project.build.directory}/jacoco-merged.exec</destFile>
                        </configuration>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                        <configuration>
                            <dataFile>${project.build.directory}/jacoco-merged.exec</dataFile>
                            <!-- Metamodel e repositories gerados pelo Hibernate Processor (Processo_, ProcessoRepository_) -->
                            <excludes>
                                <exclude>**/*_.class</exclude>
                            </excludes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <!-- mvn test -Prapido: só os testes puros e o ArchUnit, sem Docker. Os *BancoTest (@QuarkusTest) ficam de fora
             pelo nome, e não por tag, porque o Quarkus já sobe o Dev Services ao descobrir uma classe @QuarkusTest,
             antes de qualquer filtro do JUnit -->
        <profile>
            <id>rapido</id>
            <build>
                <plugins>
                    <plugin>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <configuration>
                            <excludes>
                                <exclude>**/*BancoTest.java</exclude>
                            </excludes>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

### `src/main/resources/application.properties`

```properties
quarkus.datasource.db-kind=oracle

# O schema é responsabilidade do Flyway (migrations em db/migration); o Hibernate só confere o mapeamento
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.schema-management.strategy=validate

# integracao.pdf: endereço do sistema de geração de PDF. No perfil dev não é usado (PdfClientSimulado).
quarkus.rest-client.sistema-pdf.url=${SISTEMA_PDF_URL:http://localhost:8089}
# O PDF é pedido depois do commit, mas na mesma requisição: sem timeout, um sistema lento seguraria o 201
quarkus.rest-client.sistema-pdf.connect-timeout=2000
quarkus.rest-client.sistema-pdf.read-timeout=2000

# integracao.cadastro: endereço do sistema de cadastro. No perfil dev não é usado (CadastroClientSimulado).
quarkus.rest-client.cadastro.url=${CADASTRO_URL:http://localhost:8090}
# Timeout curto (ms): a chamada roda fora da transação, mas segura a requisição; esgotado, a resposta é 503
quarkus.rest-client.cadastro.connect-timeout=2000
quarkus.rest-client.cadastro.read-timeout=2000

# Contrato da API (/q/openapi; Swagger UI em /q/swagger-ui só no dev) e health (/q/health, readiness inclui o banco)
quarkus.smallrye-openapi.info-title=Processo
quarkus.smallrye-openapi.info-description=Criação, processamento e consulta de processos de todas as modalidades

# apresentacao.mensageria: criação de processo imobiliário por fila (AMQP 1.0). Em prod o broker vem só por
# variável de ambiente (AMQP_HOST, AMQP_PORT, AMQP_USERNAME, AMQP_PASSWORD): declarar amqp-host aqui desligaria o Dev
# Services, que sobe um Artemis no dev e nos testes (imagem oficial da Apache, do Docker Hub).
quarkus.amqp.devservices.image-name=apache/activemq-artemis:2.39.0
# A imagem da Apache cria o usuário pelo ambiente; o Dev Services conecta como admin/admin
quarkus.amqp.devservices.container-env.ARTEMIS_USER=admin
quarkus.amqp.devservices.container-env.ARTEMIS_PASSWORD=admin
# Broker só de dev/teste: não bloqueia produtores quando o disco da máquina passa de 90% (o padrão do Artemis)
quarkus.amqp.devservices.container-env.JAVA_ARGS_APPEND=-Dbrokerconfig.maxDiskUsage=-1
mp.messaging.incoming.processos-imobiliarios-novos.connector=smallrye-amqp
mp.messaging.incoming.processos-imobiliarios-novos.address=processos.imobiliarios.novos
# Fila (anycast: cada mensagem para um consumidor só), não tópico; sem isso o Artemis cria o endereço como multicast
mp.messaging.incoming.processos-imobiliarios-novos.capabilities=queue
# nack (falha temporária) devolve a mensagem ao broker para ser reentregue
mp.messaging.incoming.processos-imobiliarios-novos.failure-strategy=release
mp.messaging.outgoing.processos-imobiliarios-respostas.connector=smallrye-amqp
mp.messaging.outgoing.processos-imobiliarios-respostas.address=processos.imobiliarios.respostas
mp.messaging.outgoing.processos-imobiliarios-respostas.capabilities=queue

# Cobertura dos @QuarkusTest (quarkus-jacoco, só nos testes): o relatório único é o do jacoco-maven-plugin, no verify
%test.quarkus.jacoco.report=false
```

### `docker-compose.yml`

```yaml
# Oracle para rodar o jar empacotado (em dev e nos testes o Quarkus Dev Services sobe o próprio container)
services:
  oracle:
    image: gvenzl/oracle-free:23-slim-faststart
    ports:
      - "1521:1521"
    environment:
      ORACLE_PASSWORD: oracle
      APP_USER: processo
      APP_USER_PASSWORD: processo
    healthcheck:
      test: ["CMD", "healthcheck.sh"]
      interval: 5s
      timeout: 5s
      retries: 30
```

## 16. Testes puros

Máquina de estados em tabela, regras e decisão de um tipo, integração com o cliente em lambda.

### `src/test/java/com/example/processo/modelo/SituacaoTest.java`

```java
package com.example.processo.modelo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A máquina inteira em tabela: para cada situação, cada evento leva a outra situação ou é recusado. */
class SituacaoTest {

    enum Evento {
        INICIAR_PROCESSAMENTO(Situacao::iniciarProcessamento, "processado"),
        CONCLUIR(Situacao::concluir, "concluído"),
        RECUSAR(Situacao::recusar, "recusado"),
        CANCELAR(Situacao::cancelar, "cancelado");

        final UnaryOperator<Situacao> aplicar;
        final String acao;

        Evento(UnaryOperator<Situacao> aplicar, String acao) {
            this.aplicar = aplicar;
            this.acao = acao;
        }
    }

    /** As únicas transições válidas; todo o resto deve ser recusado. */
    static final Map<Situacao, Map<Evento, Situacao>> TRANSICOES = Map.of(
            Situacao.ABERTO, Map.of(
                    Evento.INICIAR_PROCESSAMENTO, Situacao.EM_PROCESSAMENTO,
                    Evento.CANCELAR, Situacao.CANCELADO),
            Situacao.EM_PROCESSAMENTO, Map.of(
                    Evento.CONCLUIR, Situacao.CONCLUIDO,
                    Evento.RECUSAR, Situacao.RECUSADO),
            Situacao.CONCLUIDO, Map.of(),
            Situacao.RECUSADO, Map.of(),
            Situacao.CANCELADO, Map.of());

    @ParameterizedTest
    @EnumSource(Situacao.class)
    void cadaSituacaoAceitaSoAsProprias(Situacao situacao) {
        for (var evento : Evento.values()) {
            var destino = TRANSICOES.get(situacao).get(evento);
            if (destino != null) {
                assertEquals(destino, evento.aplicar.apply(situacao), situacao + " + " + evento);
            } else {
                var erro = assertThrows(TransicaoInvalidaException.class, () -> evento.aplicar.apply(situacao),
                        situacao + " + " + evento);
                assertEquals("Processo " + situacao + " não pode ser " + evento.acao, erro.getMessage());
            }
        }
    }
}
```

### `src/test/java/com/example/processo/negocio/veicular/ModalidadeVeicularTest.java`

```java
package com.example.processo.negocio.veicular;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.Valor;
import com.example.processo.negocio.processo.Avaliacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** As regras e a decisão da modalidade veicular, em Java puro, com a data fixa. */
class ModalidadeVeicularTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Clock RELOGIO = Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC);

    final ModalidadeVeicular modalidade = new ModalidadeVeicular(RELOGIO);

    static ProcessoVeicular pedido(String valor, String placa, Integer ano, String fipe) {
        return new ProcessoVeicular("Ana Souza", new Cpf("52998224725"), new Valor(valor), placa, ano,
                fipe == null ? null : new BigDecimal(fipe));
    }

    static Optional<String> violacao(ProcessoVeicular pedido) {
        return RegrasVeiculares.TODAS.violacao(pedido);
    }

    @Test
    void placaAntigaEMercosulSaoAceitas() {
        assertEquals(Optional.empty(), violacao(pedido("1000", "ABC1234", 2022, "50000")));
        assertEquals(Optional.empty(), violacao(pedido("1000", "ABC1D23", 2022, "50000")));
    }

    @Test
    void dadosDoVeiculoInvalidos() {
        assertEquals(Optional.of("Placa inválida"), violacao(pedido("1000", "abc-1234", 2022, "50000")));
        assertEquals(Optional.of("Ano de fabricação inválido"), violacao(pedido("1000", "ABC1234", null, "50000")));
        assertEquals(Optional.of("Valor FIPE deve ser maior que zero"), violacao(pedido("1000", "ABC1234", 2022, "0")));
        assertEquals(Optional.of("Valor excede 90% da FIPE (máximo 45000.00)"),
                violacao(pedido("45000.01", "ABC1234", 2022, "50000")));
    }

    @Test
    void processamentoRecusaVeiculoComMaisDeDezAnos() {
        assertEquals(Avaliacao.aprovado(), modalidade.avaliar(pedido("1000", "ABC1234", 2016, "50000")));
        assertEquals(Avaliacao.recusado("Veículo com mais de 10 anos (fabricado em 2015)"),
                modalidade.avaliar(pedido("1000", "ABC1234", 2015, "50000")));
    }
}
```

### `src/test/java/com/example/processo/integracao/cadastro/CadastroIntegracaoTest.java`

```java
package com.example.processo.integracao.cadastro;

import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A tradução da resposta externa; o cliente é substituído por um que responde fixo. A chamada HTTP real é coberta nos ITs. */
class CadastroIntegracaoTest {

    static CadastroIntegracao respondendo(String situacao) {
        return new CadastroIntegracao(documento -> new RespostaCadastro(documento, situacao));
    }

    @Test
    void regularSoQuandoOSistemaRespondeRegular() {
        assertTrue(respondendo("REGULAR").regular("52998224725"));
        assertFalse(respondendo("SUSPENSA").regular("52998224725"));
        assertFalse(respondendo(null).regular("52998224725"));
    }

    @Test
    void simulacaoDoDevSempreRespondeRegular() {
        assertTrue(new CadastroIntegracao(new CadastroClientSimulado()).regular("52998224725"));
    }

    @Test
    void falhaDoClienteViraIndisponibilidade() {
        var foraDoAr = new CadastroIntegracao(documento -> {
            throw new ProcessingException("Connection refused");
        });
        var erroHttp = new CadastroIntegracao(documento -> {
            throw new WebApplicationException(500);
        });

        assertThrows(SistemaExternoIndisponivelException.class, () -> foraDoAr.regular("52998224725"));
        assertThrows(SistemaExternoIndisponivelException.class, () -> erroHttp.regular("52998224725"));
    }
}
```

## 17. Testes com o banco (@QuarkusTest, *BancoTest)

O motor e os repositories no Oracle do Dev Services, com o mesmo conjunto de @WithTestResource.

### `src/test/java/com/example/processo/negocio/processo/ProcessoServiceBancoTest.java`

```java
package com.example.processo.negocio.processo;

import com.example.processo.CadastroFalso;
import com.example.processo.FilaEmMemoria;
import com.example.processo.SistemaDePdfFalso;
import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.Situacao;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.TransicaoInvalidaException;
import com.example.processo.modelo.Valor;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O motor com o banco de verdade (Oracle do Dev Services) e os sistemas externos falsos. Cada teste usa matrículas e
 * chaves próprias: o banco é compartilhado com os outros testes.
 */
@QuarkusTest
// O mesmo conjunto de recursos dos outros @QuarkusTest: o app (e o Oracle) sobe uma vez só
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class ProcessoServiceBancoTest {

    static final String ANO = Integer.toString(LocalDate.now().getYear());

    @Inject
    ProcessoService motor;

    @Inject
    ProcessoRepository repositorio;

    static String matricula() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
    }

    static ProcessoImobiliario imobiliario(String nome, String cpf, String matricula) {
        return new ProcessoImobiliario(nome, new Cpf(cpf), new Valor("400000.00"), matricula, TipoImovel.RESIDENCIAL,
                new BigDecimal("500000.00"), "SP");
    }

    static ProcessoImobiliario imobiliario(String matricula) {
        return imobiliario("Ana Souza", "52998224725", matricula);
    }

    static ProcessoVeicular veicular(String cpf, String placa, int anoFabricacao) {
        return new ProcessoVeicular("Ana Souza", new Cpf(cpf), new Valor("45000.00"), placa, anoFabricacao,
                new BigDecimal("50000.00"));
    }

    static List<String> pedidosDePdf(String numero) {
        return SistemaDePdfFalso.PEDIDOS.stream().filter(p -> p.contains("\"referencia\":\"" + numero + "\"")).toList();
    }

    @Test
    void criaComONumeroDaModalidadeGravaEPedeOPdf() {
        var imovel = motor.criar(imobiliario(matricula()));
        var veiculo = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 2));

        assertTrue(imovel.getNumero().matches("IMO-" + ANO + "-\\d{6}"), imovel.getNumero());
        assertTrue(veiculo.getNumero().matches("VEI-" + ANO + "-\\d{6}"), veiculo.getNumero());
        var gravado = assertInstanceOf(ProcessoImobiliario.class, repositorio.buscar(imovel.getId()).orElseThrow());
        assertEquals(Situacao.ABERTO, gravado.getSituacao());
        assertEquals(imovel.getMatricula(), gravado.getMatricula());
        assertEquals(1, pedidosDePdf(imovel.getNumero()).size());
        assertEquals(1, pedidosDePdf(veiculo.getNumero()).size());
        // Só os atributos da modalidade: nada do que o enhancement do Hibernate acrescenta à entidade no build
        Map<String, Object> campos = new JsonObject(pedidosDePdf(imovel.getNumero()).getFirst()).getJsonObject("campos").getMap();
        assertEquals(Set.of("matricula", "tipoImovel", "valorAvaliacao", "uf"), campos.keySet());
    }

    @Test
    void mesmaChaveDevolveOProcessoJaCriadoSemPassarPelasRegras() {
        var chave = "chave-" + UUID.randomUUID();
        var matricula = matricula();

        var primeiro = motor.criar(imobiliario(matricula), chave);
        // A mesma matrícula seria recusada pela regra de processo em andamento: a chave devolve antes das regras
        var reentregue = motor.criar(imobiliario(matricula), chave);

        assertEquals(primeiro.getId(), reentregue.getId());
        assertEquals(primeiro.getNumero(), reentregue.getNumero());
        assertEquals(1, pedidosDePdf(primeiro.getNumero()).size(), "não pede outro PDF");
    }

    @Test
    void recusaNaoRegistraAChave() {
        var chave = "chave-" + UUID.randomUUID();

        assertThrows(RegraVioladaException.class, () -> motor.criar(imobiliario(" ", "52998224725", matricula()), chave));

        assertEquals(Optional.empty(), repositorio.processoDaChave(chave), "a mesma mensagem corrigida ainda pode criar");
    }

    @Test
    void regrasComunsVemAntesDasDaModalidade() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(imobiliario(" ", "52998224725", "matricula-invalida")));

        assertEquals("Nome do proponente é obrigatório", erro.getMessage());
    }

    @Test
    void sistemaDeCadastroSoEhConsultadoSeAsOutrasRegrasPassarem() {
        // Com a placa inválida, a regra recusa antes de chamar o cadastro (que, para este CPF, está fora do ar)
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_SISTEMA_FORA, "placa", 2022)));
        assertEquals("Placa inválida", erro.getMessage());

        assertThrows(SistemaExternoIndisponivelException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_SISTEMA_FORA, "ABC1D23", 2022)));
    }

    @Test
    void situacaoCadastralIrregularRecusa() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_IRREGULAR, "ABC1D23", 2022)));

        assertEquals("Situação cadastral irregular para o CPF " + CadastroFalso.CPF_IRREGULAR, erro.getMessage());
    }

    @Test
    void falhaNoPdfNaoDesfazACriacao() {
        var matricula = SistemaDePdfFalso.FALHA + ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);

        var processo = motor.criar(imobiliario(matricula));

        assertEquals(1, pedidosDePdf(processo.getNumero()).size());
        assertEquals(Situacao.ABERTO, repositorio.buscar(processo.getId()).orElseThrow().getSituacao());
    }

    @Test
    void cicloDeVidaGravaCadaSituacao() {
        var aprovado = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 2));
        var recusado = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 11));
        var cancelado = motor.criar(veicular("52998224725", "ABC1D23", 2022));

        assertEquals(Situacao.CONCLUIDO, motor.processar(aprovado.getId()).getSituacao());
        var processado = motor.processar(recusado.getId());
        motor.cancelar(cancelado.getId());

        assertEquals(Situacao.CONCLUIDO, repositorio.buscar(aprovado.getId()).orElseThrow().getSituacao());
        var gravadoRecusado = repositorio.buscar(recusado.getId()).orElseThrow();
        assertEquals(Situacao.RECUSADO, gravadoRecusado.getSituacao());
        assertEquals(processado.getMotivoRecusa(), gravadoRecusado.getMotivoRecusa());
        assertEquals(Situacao.CANCELADO, repositorio.buscar(cancelado.getId()).orElseThrow().getSituacao());
    }

    @Test
    void transicaoInvalidaNaoGrava() {
        var processo = motor.criar(veicular("52998224725", "ABC1D23", 2022));
        motor.cancelar(processo.getId());

        var erro = assertThrows(TransicaoInvalidaException.class, () -> motor.processar(processo.getId()));

        assertEquals("Processo CANCELADO não pode ser processado", erro.getMessage());
        assertEquals(Situacao.CANCELADO, repositorio.buscar(processo.getId()).orElseThrow().getSituacao());
    }

    @Test
    void processoInexistente() {
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.processar(UUID.randomUUID()));
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.cancelar(UUID.randomUUID()));
    }
}
```

### `src/test/java/com/example/processo/persistencia/ProcessoRepositoryBancoTest.java`

```java
package com.example.processo.persistencia;

import com.example.processo.CadastroFalso;
import com.example.processo.FilaEmMemoria;
import com.example.processo.SistemaDePdfFalso;
import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.Situacao;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;
import com.example.processo.persistencia.repositorio.ProcessoImobiliarioRepository;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Os repositories no Oracle: o que o negócio espera da gravação polimórfica, do compare-and-set e da chave de
 * idempotência. Cada teste grava processos próprios (número e matrícula aleatórios): o banco é compartilhado.
 */
@QuarkusTest
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class ProcessoRepositoryBancoTest {

    @Inject
    ProcessoRepository processos;

    @Inject
    ProcessoImobiliarioRepository imobiliarios;

    static String aleatorio() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
    }

    ProcessoImobiliario gravado(String matricula, String uf) {
        var processo = new ProcessoImobiliario("Ana Souza", new Cpf("52998224725"), new Valor("400000.00"), matricula,
                TipoImovel.RESIDENCIAL, new BigDecimal("500000.00"), uf);
        processo.abrir("TST-" + aleatorio(), LocalDate.of(2026, 10, 19));
        QuarkusTransaction.requiringNew().run(() -> processos.inserir(processo));
        return processo;
    }

    boolean mudar(ProcessoImobiliario processo, Situacao de, Situacao para, String motivo) {
        return QuarkusTransaction.requiringNew().call(() -> processos.mudarSituacao(processo.getId(), de, para, motivo));
    }

    @Test
    void buscarPeloPaiDevolveAFilhaCompleta() {
        var processo = gravado(aleatorio(), "SP");

        var lido = assertInstanceOf(ProcessoImobiliario.class, processos.buscar(processo.getId()).orElseThrow());

        assertEquals(ProcessoImobiliario.SEGMENTO, lido.getSegmento());
        assertEquals(processo.getNumero(), lido.getNumero());
        assertEquals("52998224725", lido.getProponenteCpf());
        assertEquals(Situacao.ABERTO, lido.getSituacao());
        assertEquals(processo.getMatricula(), lido.getMatricula());
        assertEquals(TipoImovel.RESIDENCIAL, lido.getTipoImovel());
        // NUMBER do Oracle não guarda zeros à direita: compare números, não texto
        assertEquals(0, new BigDecimal("400000.00").compareTo(lido.getValor()));
        assertEquals(0, new BigDecimal("500000.00").compareTo(lido.getValorAvaliacao()));
        assertEquals(Optional.empty(), processos.buscar(UUID.randomUUID()));
    }

    @Test
    void mudarSituacaoSoQuandoAindaEstaNaAnterior() {
        var processo = gravado(aleatorio(), "SP");

        assertTrue(mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null));
        assertFalse(mudar(processo, Situacao.ABERTO, Situacao.CANCELADO, null), "compare-and-set: já não está ABERTO");
        assertTrue(mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.RECUSADO, "Valor alto"));

        var lido = processos.buscar(processo.getId()).orElseThrow();
        assertEquals(Situacao.RECUSADO, lido.getSituacao());
        assertEquals("Valor alto", lido.getMotivoRecusa());
    }

    @Test
    void chaveDeIdempotenciaLigaAoProcessoUmaVezSo() {
        var chave = "msg-" + aleatorio();
        var processo = gravado(aleatorio(), "SP");
        var outro = gravado(aleatorio(), "SP");

        assertEquals(Optional.empty(), processos.processoDaChave(chave));

        QuarkusTransaction.requiringNew().run(() -> processos.registrarChave(chave, processo.getId()));

        assertEquals(Optional.of(processo.getId()), processos.processoDaChave(chave));
        assertThrows(RuntimeException.class, () -> QuarkusTransaction.requiringNew()
                .run(() -> processos.registrarChave(chave, outro.getId())));
        assertEquals(Optional.of(processo.getId()), processos.processoDaChave(chave));
    }

    @Test
    void emAndamentoSoAbertoOuEmProcessamentoNaMesmaMatriculaEUf() {
        var matricula = aleatorio();
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"));

        var processo = gravado(matricula, "SP");
        assertTrue(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "ABERTO");
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "RJ"), "outra UF");

        mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null);
        assertTrue(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "EM_PROCESSAMENTO");

        mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.CONCLUIDO, null);
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "CONCLUIDO não está em andamento");
    }
}
```

### `src/test/java/com/example/processo/NovosProcessosPorFilaBancoTest.java`

```java
package com.example.processo;

import com.example.processo.apresentacao.mensageria.dto.RespostaNovoProcesso;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O consumidor da fila de novos processos, com os canais em memória (sem broker) e o banco real: o que vira
 * resposta, o que é confirmado (ack) e o que volta para reentrega (nack).
 */
@QuarkusTest
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class NovosProcessosPorFilaBancoTest {

    @Inject
    @Any
    InMemoryConnector fila;

    @Inject
    ProcessoConsultas consultas;

    InMemorySink<RespostaNovoProcesso> respostas;

    @BeforeEach
    void limpar() {
        respostas = fila.sink(FilaEmMemoria.RESPOSTAS);
        respostas.clear();
    }

    static JsonObject mensagem(String id, String cpf) {
        var matricula = Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
        return new JsonObject()
                .put("id", id).put("nome", "Ana Souza").put("cpf", cpf).put("valor", "400000.00")
                .put("matricula", matricula).put("tipoImovel", "RESIDENCIAL").put("valorAvaliacao", "500000.00")
                .put("uf", "SP");
    }

    /** Envia e devolve como a mensagem terminou: "ack" ou "nack". */
    CompletableFuture<String> enviar(Object corpo) {
        var desfecho = new CompletableFuture<String>();
        // Envia uma Message (não só o payload) para observar o ack/nack que o consumidor dá
        fila.<Message<Object>>source(FilaEmMemoria.NOVOS).send(Message.<Object>of(corpo,
                () -> {
                    desfecho.complete("ack");
                    return CompletableFuture.completedFuture(null);
                },
                erro -> {
                    desfecho.complete("nack");
                    return CompletableFuture.completedFuture(null);
                }));
        return desfecho;
    }

    static String esperar(CompletableFuture<String> desfecho) throws Exception {
        return desfecho.get(30, TimeUnit.SECONDS);
    }

    List<RespostaNovoProcesso> respostasDe(String id) {
        return respostas.received().stream().map(Message::getPayload).filter(r -> id.equals(r.idMensagem())).toList();
    }

    @Test
    void mensagemValidaCriaOProcessoERespondeComONumero() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "52998224725"))));

        var resposta = respostasDe(id).getFirst();
        assertTrue(resposta.criado());
        assertTrue(resposta.numero().startsWith("IMO-"));
        assertEquals(resposta.numero(), consultas.imobiliario(resposta.processoId()).orElseThrow().numero());
    }

    @Test
    void jsonComoTextoTambemValeComoChegaDoMainframe() throws Exception {
        // Sem content-type JSON, o connector AMQP entrega o corpo como texto
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "52998224725").encode())));

        assertTrue(respostasDe(id).getFirst().criado());
    }

    @Test
    void mensagemReentregueNaoDuplicaOProcesso() throws Exception {
        var id = "msg-" + UUID.randomUUID();
        var corpo = mensagem(id, "52998224725");

        assertEquals("ack", esperar(enviar(corpo)));
        assertEquals("ack", esperar(enviar(corpo)));

        var recebidas = respostasDe(id);
        assertEquals(2, recebidas.size());
        assertEquals(recebidas.get(0).processoId(), recebidas.get(1).processoId());
        assertEquals(recebidas.get(0).numero(), recebidas.get(1).numero());
    }

    @Test
    void regraVioladaRespondeRecusadoEConfirma() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "12345678900"))));

        var resposta = respostasDe(id).getFirst();
        assertFalse(resposta.criado());
        assertEquals("CPF inválido: 12345678900", resposta.motivo());
    }

    @Test
    void mensagemMalformadaRespondeRecusadoEConfirma() throws Exception {
        var antes = respostas.received().size();

        assertEquals("ack", esperar(enviar(new JsonObject().put("id", "x").put("tipoImovel", "CASTELO"))));

        var resposta = respostas.received().get(antes).getPayload();
        assertFalse(resposta.criado());
        assertEquals("Mensagem malformada", resposta.motivo());
    }

    @Test
    void sistemaExternoForaDoArDevolveParaReentrega() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("nack", esperar(enviar(mensagem(id, CadastroFalso.CPF_SISTEMA_FORA))));

        assertTrue(respostasDe(id).isEmpty(), "sem resposta: a mensagem volta para a fila");
    }
}
```

## 18. Infraestrutura de teste

Sistemas externos falsos (servidor HTTP na JVM do teste) e a fila em memória.

### `src/test/java/com/example/processo/CadastroFalso.java`

```java
package com.example.processo;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Faz o papel do sistema externo de cadastro nos ITs. O jar roda no perfil prod, sem a simulação do dev, então a
 * chamada é HTTP de verdade pelo REST Client: GET /situacoes/{documento} responde SUSPENSA para
 * {@link #CPF_IRREGULAR}, erro 500 para {@link #CPF_SISTEMA_FORA} e REGULAR para os demais.
 */
public class CadastroFalso implements QuarkusTestResourceLifecycleManager {

    public static final String CPF_IRREGULAR = "11144477735";
    public static final String CPF_SISTEMA_FORA = "39053344705";

    private HttpServer servidor;

    @Override
    public Map<String, String> start() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/situacoes/", troca -> {
            var documento = troca.getRequestURI().getPath().substring("/situacoes/".length());
            if (documento.equals(CPF_SISTEMA_FORA)) {
                troca.sendResponseHeaders(500, -1);
                troca.close();
                return;
            }
            var situacao = documento.equals(CPF_IRREGULAR) ? "SUSPENSA" : "REGULAR";
            var corpo = "{\"documento\":\"%s\",\"situacao\":\"%s\"}".formatted(documento, situacao)
                    .getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(200, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();
        return Map.of("quarkus.rest-client.cadastro.url", "http://localhost:" + servidor.getAddress().getPort());
    }

    @Override
    public void stop() {
        servidor.stop(0);
    }
}
```

### `src/test/java/com/example/processo/SistemaDePdfFalso.java`

```java
package com.example.processo;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Faz o papel do sistema de geração de PDF nos testes: guarda cada pedido recebido em /documentos e responde 204,
 * ou 500 quando a matrícula do imóvel começa com {@link #FALHA}. O jar testado recebe o endereço pela
 * configuração devolvida por {@link #start()}.
 */
public class SistemaDePdfFalso implements QuarkusTestResourceLifecycleManager {

    public static final String FALHA = "999";
    public static final List<String> PEDIDOS = new CopyOnWriteArrayList<>();

    private HttpServer servidor;

    @Override
    public Map<String, String> start() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/documentos", troca -> {
            var corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            PEDIDOS.add(corpo);
            troca.sendResponseHeaders(corpo.contains("\"matricula\":\"" + FALHA) ? 500 : 204, -1);
            troca.close();
        });
        servidor.start();
        return Map.of("quarkus.rest-client.sistema-pdf.url", "http://localhost:" + servidor.getAddress().getPort());
    }

    @Override
    public void stop() {
        servidor.stop(0);
    }
}
```

### `src/test/java/com/example/processo/FilaEmMemoria.java`

```java
package com.example.processo;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

/** Troca os canais da fila por memória: o teste do consumidor roda sem broker (e o Dev Services do AMQP não sobe). */
public class FilaEmMemoria implements QuarkusTestResourceLifecycleManager {

    static final String NOVOS = "processos-imobiliarios-novos";
    static final String RESPOSTAS = "processos-imobiliarios-respostas";

    @Override
    public Map<String, String> start() {
        var config = new HashMap<String, String>();
        config.putAll(InMemoryConnector.switchIncomingChannelsToInMemory(NOVOS));
        config.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory(RESPOSTAS));
        return config;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
```

## 19. Testes de integração (contra o jar)

O mesmo ciclo de vida para cada tipo, inclusive concorrência.

### `src/test/java/com/example/processo/CicloDeVidaPorTipoIT.java`

```java
package com.example.processo;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O mesmo cenário para cada tipo de processo: a máquina de estados é do motor e vale igual para todos. Tipo novo
 * entra só em {@link #tipos()}.
 */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
@WithTestResource(CadastroFalso.class)
class CicloDeVidaPorTipoIT {

    /** Onde o tipo é criado e consultado, um corpo que o processamento aprova e outro que ele recusa. */
    record Tipo(String nome, String caminho, Supplier<Map<String, Object>> aprovavel,
                Supplier<Map<String, Object>> recusavel, String motivoDaRecusa) {

        @Override
        public String toString() {
            return nome;
        }
    }

    static final Map<String, String> PROPONENTE = Map.of("nome", "Ana Souza", "cpf", "52998224725");

    static final Tipo IMOBILIARIO = new Tipo("IMOBILIARIO", "/processos/imobiliarios",
            () -> imobiliario("400000.00", "500000.00"),
            () -> imobiliario("1200000.00", "2000000.00"),
            "Valor acima de 1000000.00 exige análise manual");

    static final Tipo VEICULAR = new Tipo("VEICULAR", "/processos/veiculares",
            () -> veicular(LocalDate.now().getYear() - 2),
            () -> veicular(LocalDate.now().getYear() - 11),
            "Veículo com mais de 10 anos (fabricado em " + (LocalDate.now().getYear() - 11) + ")");

    static Stream<Tipo> tipos() {
        return Stream.of(IMOBILIARIO, VEICULAR);
    }

    static Map<String, Object> imobiliario(String valor, String avaliacao) {
        var matricula = Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
        return Map.of("proponente", PROPONENTE, "valor", valor,
                "imovel", Map.of("matricula", matricula, "tipo", "RESIDENCIAL", "valorAvaliacao", avaliacao, "uf", "SP"));
    }

    static Map<String, Object> veicular(int anoFabricacao) {
        return Map.of("proponente", PROPONENTE, "valor", "45000.00",
                "veiculo", Map.of("placa", "ABC1D23", "anoFabricacao", anoFabricacao, "valorFipe", "50000.00"));
    }

    static String criar(Tipo tipo, Map<String, Object> corpo) {
        return given().contentType(ContentType.JSON).body(corpo).post(tipo.caminho())
                .then().statusCode(201).extract().path("id");
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void aprovadoTerminaConcluidoEFinal(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());

        given().post("/processos/" + id + "/processamento").then().statusCode(200)
                .body("situacao", equalTo("CONCLUIDO"))
                .body("motivo", nullValue());

        given().get(tipo.caminho() + "/" + id).then().statusCode(200).body("situacao", equalTo("CONCLUIDO"));
        given().post("/processos/" + id + "/processamento").then().statusCode(422)
                .body("detail", equalTo("Processo CONCLUIDO não pode ser processado"));
        given().post("/processos/" + id + "/cancelamento").then().statusCode(422)
                .body("detail", equalTo("Processo CONCLUIDO não pode ser cancelado"));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void recusadoGuardaOMotivoDoTipo(Tipo tipo) {
        var id = criar(tipo, tipo.recusavel().get());

        given().post("/processos/" + id + "/processamento").then().statusCode(200)
                .body("situacao", equalTo("RECUSADO"))
                .body("motivo", equalTo(tipo.motivoDaRecusa()));

        given().get(tipo.caminho() + "/" + id).then().statusCode(200)
                .body("situacao", equalTo("RECUSADO"))
                .body("motivoRecusa", equalTo(tipo.motivoDaRecusa()));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void canceladoNaoPodeSerProcessado(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());

        given().post("/processos/" + id + "/cancelamento").then().statusCode(204);

        given().get(tipo.caminho() + "/" + id).then().body("situacao", equalTo("CANCELADO"));
        given().post("/processos/" + id + "/processamento").then().statusCode(422)
                .body("detail", equalTo("Processo CANCELADO não pode ser processado"));
    }

    /**
     * Várias requisições de processamento para o mesmo processo, soltas ao mesmo tempo. Só uma pode ganhar a troca
     * ABERTO → EM_PROCESSAMENTO (update condicional no Oracle). As outras recebem 409, se tentaram o update
     * junto com a vencedora, ou 422, se já leram o processo em EM_PROCESSAMENTO ou CONCLUIDO.
     */
    @ParameterizedTest
    @MethodSource("tipos")
    void processamentoSimultaneoSoAconteceUmaVez(Tipo tipo) throws Exception {
        int requisicoes = 8;
        var id = criar(tipo, tipo.aprovavel().get());
        var largada = new CountDownLatch(1);
        var respostas = new ArrayList<Future<Integer>>();

        try (var executor = Executors.newFixedThreadPool(requisicoes)) {
            for (int i = 0; i < requisicoes; i++) {
                respostas.add(executor.submit(() -> {
                    largada.await();
                    return given().post("/processos/" + id + "/processamento").then().extract().statusCode();
                }));
            }
            largada.countDown();
            var status = new ArrayList<Integer>();
            for (var resposta : respostas) {
                status.add(resposta.get(30, TimeUnit.SECONDS));
            }

            var resumo = tipo + " " + status;
            System.out.println("Processamento simultâneo: " + resumo);
            assertEquals(1, status.stream().filter(s -> s == 200).count(), "exatamente um 200: " + resumo);
            assertTrue(status.stream().allMatch(s -> s == 200 || s == 409 || s == 422), "só 200, 409 ou 422: " + resumo);
        }
        given().get(tipo.caminho() + "/" + id).then().body("situacao", equalTo("CONCLUIDO"));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void listagemTrazOTipoComASituacaoAtual(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());
        given().post("/processos/" + id + "/processamento").then().statusCode(200);

        var item = Listagem.todos("CONCLUIDO").stream().filter(i -> id.equals(i.get("id"))).findFirst()
                .orElseThrow(() -> new AssertionError("Processo " + id + " não está na listagem"));
        assertEquals(tipo.nome(), item.get("segmento"));
    }

    @Test
    void processoInexistente() {
        var inexistente = "00000000-0000-0000-0000-000000000000";
        given().post("/processos/" + inexistente + "/processamento").then().statusCode(404);
        given().post("/processos/" + inexistente + "/cancelamento").then().statusCode(404);
    }
}
```

### `src/test/java/com/example/processo/Listagem.java`

```java
package com.example.processo;

import io.restassured.path.json.JsonPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/** Lê a listagem paginada inteira, página a página: os ITs compartilham o banco e não sabem em que página cai o seu processo. */
final class Listagem {

    private Listagem() {
    }

    /** Todos os itens de {@code GET /processos}, com o filtro de situação se não for nulo. */
    static List<Map<String, Object>> todos(String situacao) {
        var itens = new ArrayList<Map<String, Object>>();
        for (int pagina = 1; ; pagina++) {
            var requisicao = given().queryParam("pagina", pagina).queryParam("tamanho", 100);
            if (situacao != null) {
                requisicao.queryParam("situacao", situacao);
            }
            JsonPath resposta = requisicao.get("/processos").then().statusCode(200).extract().jsonPath();
            List<Map<String, Object>> daPagina = resposta.getList("itens");
            itens.addAll(daPagina);
            if (daPagina.isEmpty() || itens.size() >= resposta.getLong("total")) {
                return itens;
            }
        }
    }

    static Map<String, Object> porNumero(List<Map<String, Object>> itens, String numero) {
        return itens.stream().filter(i -> numero.equals(i.get("numero"))).findFirst()
                .orElseThrow(() -> new AssertionError("Processo " + numero + " não está na listagem"));
    }
}
```
