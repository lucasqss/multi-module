-- Dono: dominio.modalidade.servico (plug-in). Faixa V1_30 a V1_39.
CREATE TABLE svc_processo (
    id                 NUMBER PRIMARY KEY REFERENCES prc_processo (id),
    possui_cronograma  CHAR(1) DEFAULT 'N' NOT NULL CHECK (possui_cronograma IN ('S', 'N'))
);

CREATE TABLE svc_objeto (
    id              NUMBER PRIMARY KEY REFERENCES prc_objeto (id),
    escopo          VARCHAR2(1000) NOT NULL,
    prazo_execucao  DATE
);
