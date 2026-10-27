-- Cadastro simples (CRUD), sem relação com o fluxo de processos: o exemplo do caminho curto (ADR 0012)
CREATE TABLE AGENCIA (
    CODIGO VARCHAR2(4)   NOT NULL,
    NOME   VARCHAR2(100) NOT NULL,
    UF     VARCHAR2(2)   NOT NULL,
    ATIVA  NUMBER(1)     NOT NULL,
    CONSTRAINT PK_AGENCIA PRIMARY KEY (CODIGO),
    CONSTRAINT CK_AGENCIA_ATIVA CHECK (ATIVA IN (0, 1))
);
