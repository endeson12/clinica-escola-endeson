-- V1: estrutura principal da Clinica-Escola
CREATE TABLE unidades (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(120) NOT NULL,
    codigo  VARCHAR(30)  NOT NULL,
    CONSTRAINT uk_unidades_codigo UNIQUE (codigo)
);

CREATE TABLE usuarios (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE usuarios_unidades (
    usuario_id  BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    unidade_id  BIGINT NOT NULL REFERENCES unidades (id) ON DELETE RESTRICT,
    PRIMARY KEY (usuario_id, unidade_id)
);

CREATE TABLE pacientes (
    id               BIGSERIAL PRIMARY KEY,
    unidade_id       BIGINT       NOT NULL REFERENCES unidades (id) ON DELETE RESTRICT,
    nome_completo    VARCHAR(160) NOT NULL,
    cpf              CHAR(11)     NOT NULL,
    email            VARCHAR(160) NOT NULL,
    telefone         VARCHAR(20),
    data_nascimento  DATE,
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_pacientes_unidade_cpf UNIQUE (unidade_id, cpf),
    CONSTRAINT ck_pacientes_cpf_digitos CHECK (cpf ~ '^[0-9]{11}$')
);

CREATE TABLE especialidades (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    descricao  VARCHAR(500),
    ativa      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
-- nome unico sem diferenciar maiusculas/minusculas
CREATE UNIQUE INDEX uk_especialidades_nome ON especialidades (lower(nome));

CREATE TABLE atendimentos (
    id                BIGSERIAL PRIMARY KEY,
    unidade_id        BIGINT      NOT NULL REFERENCES unidades (id) ON DELETE RESTRICT,
    paciente_id       BIGINT      NOT NULL REFERENCES pacientes (id) ON DELETE RESTRICT,
    especialidade_id  BIGINT      NOT NULL REFERENCES especialidades (id) ON DELETE RESTRICT,
    data_hora         TIMESTAMP   NOT NULL,
    status            VARCHAR(20) NOT NULL,
    observacoes       VARCHAR(1000),
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_atendimentos_status CHECK (status IN ('AGENDADO', 'REALIZADO', 'CANCELADO'))
);

CREATE INDEX ix_pacientes_unidade ON pacientes (unidade_id);
CREATE INDEX ix_atendimentos_unidade_data ON atendimentos (unidade_id, data_hora DESC);
