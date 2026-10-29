-- Dono: dominio.modalidade.imovel (plug-in). Faixa V1_20 a V1_29. Subtabelas JOINED do núcleo.
CREATE TABLE imv_processo (
    id              NUMBER PRIMARY KEY REFERENCES prc_processo (id),
    tipo_operacao   VARCHAR2(20) NOT NULL,
    municipio_itbi  VARCHAR2(100)
);

CREATE TABLE imv_objeto (
    id                   NUMBER PRIMARY KEY REFERENCES prc_objeto (id),
    matricula            VARCHAR2(30)  NOT NULL,
    cartorio_ri          VARCHAR2(100) NOT NULL,
    inscricao_iptu       VARCHAR2(40),
    uf                   CHAR(2),
    municipio            VARCHAR2(100),
    construcao_averbada  CHAR(1) DEFAULT 'N' NOT NULL CHECK (construcao_averbada IN ('S', 'N'))
);
