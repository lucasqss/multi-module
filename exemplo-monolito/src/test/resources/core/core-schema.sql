-- Simula o core de consórcio: dono de grupo, cota e saldo. Executado como usuário CORE.
-- Cada comando termina com uma linha contendo apenas "/".
CREATE TABLE grupo (
    codigo             VARCHAR2(10) PRIMARY KEY,
    categoria          VARCHAR2(20) NOT NULL,
    regime_normativo   VARCHAR2(10) NOT NULL,
    data_primeira_ago  DATE NOT NULL
)
/
CREATE TABLE cota (
    cota_ref          VARCHAR2(30) PRIMARY KEY,
    grupo_codigo      VARCHAR2(10) NOT NULL REFERENCES grupo (codigo),
    cpf_titular       VARCHAR2(14) NOT NULL,
    contemplada       CHAR(1) NOT NULL,
    saldo_utilizavel  NUMBER(15, 2) NOT NULL
)
/
CREATE TABLE utilizacao (
    id                  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cota_ref            VARCHAR2(30) NOT NULL REFERENCES cota (cota_ref),
    valor               NUMBER(15, 2) NOT NULL,
    referencia_externa  VARCHAR2(100) NOT NULL UNIQUE,
    registrado_em       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
)
/
-- Contrato publicado pelo core para leitura: a aplicação nunca lê as tabelas acima.
CREATE OR REPLACE VIEW v_cota_credito AS
SELECT c.cota_ref, g.codigo AS grupo, g.categoria, g.regime_normativo, c.cpf_titular, c.contemplada, c.saldo_utilizavel
  FROM cota c
  JOIN grupo g ON g.codigo = c.grupo_codigo
/
-- Contrato publicado pelo core para escrita. Idempotente pela referência externa.
CREATE OR REPLACE PROCEDURE prc_informar_utilizacao (
    p_cota_ref    IN VARCHAR2,
    p_valor       IN NUMBER,
    p_referencia  IN VARCHAR2
) AS
    v_existe NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_existe FROM utilizacao WHERE referencia_externa = p_referencia;
    IF v_existe > 0 THEN
        RETURN;
    END IF;
    UPDATE cota SET saldo_utilizavel = saldo_utilizavel - p_valor WHERE cota_ref = p_cota_ref;
    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Cota inexistente: ' || p_cota_ref);
    END IF;
    INSERT INTO utilizacao (cota_ref, valor, referencia_externa) VALUES (p_cota_ref, p_valor, p_referencia);
END;
/
INSERT INTO grupo VALUES ('1001', 'IMOVEL', 'RES_285', DATE '2024-08-15')
/
INSERT INTO grupo VALUES ('2001', 'SERVICO', 'RES_285', DATE '2024-09-10')
/
INSERT INTO cota VALUES ('1001-0042', '1001', '11122233344', 'S', 300000)
/
INSERT INTO cota VALUES ('1001-0043', '1001', '11122233344', 'N', 300000)
/
INSERT INTO cota VALUES ('2001-0007', '2001', '11122233344', 'S', 40000)
/
GRANT SELECT ON v_cota_credito TO core_integracao
/
GRANT EXECUTE ON prc_informar_utilizacao TO core_integracao
/
