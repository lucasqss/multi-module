-- Chave de idempotência de quem pediu a criação (ex.: id da mensagem da fila). A PK garante que a mesma chave não
-- cria dois processos, mesmo com duas entregas simultâneas da mesma mensagem.
CREATE TABLE PROCESSO_IDEMPOTENCIA (
    CHAVE       VARCHAR2(100) NOT NULL,
    PROCESSO_ID RAW(16)       NOT NULL,
    CONSTRAINT PK_PROCESSO_IDEMPOTENCIA PRIMARY KEY (CHAVE),
    CONSTRAINT FK_PROCESSO_IDEMPOTENCIA FOREIGN KEY (PROCESSO_ID) REFERENCES PROCESSO (ID)
);
