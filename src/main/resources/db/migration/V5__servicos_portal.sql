-- Additive services: private profile, internal requests, hours review and timetable.
CREATE TABLE academico_dados_pessoais (
 usuario_id BIGINT NOT NULL, unidade_id BIGINT NOT NULL,
 telefone VARCHAR(25) NOT NULL DEFAULT '', cep VARCHAR(12) NOT NULL DEFAULT '',
 endereco VARCHAR(240) NOT NULL DEFAULT '', numero VARCHAR(20) NOT NULL DEFAULT '',
 complemento VARCHAR(120) NOT NULL DEFAULT '', bairro VARCHAR(100) NOT NULL DEFAULT '',
 cidade VARCHAR(100) NOT NULL DEFAULT '', estado VARCHAR(2) NOT NULL DEFAULT '',
 escola VARCHAR(160) NOT NULL DEFAULT '', ano_conclusao INT CHECK(ano_conclusao BETWEEN 1900 AND 2100),
 atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(usuario_id,unidade_id),
 FOREIGN KEY(usuario_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE TABLE academico_solicitacoes (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL, usuario_id BIGINT NOT NULL,
 tipo VARCHAR(30) NOT NULL CHECK(tipo IN ('DOCUMENTOS','SEGUNDA_CHAMADA','DISPENSA','MUDANCA_CURSO','TRANSFERENCIA','MUDANCA_TURMA','MUDANCA_TURNO','INCLUSAO_DISCIPLINA','CERTIFICADOS','ATENDIMENTO','OUTROS')),
 assunto VARCHAR(160) NOT NULL, mensagem VARCHAR(4000) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'ABERTA' CHECK(status IN ('ABERTA','RESPONDIDA','CANCELADA')),
 resposta VARCHAR(4000), respondido_por BIGINT REFERENCES usuarios(id), respondido_em TIMESTAMPTZ,
 criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(usuario_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE INDEX ix_solicitacoes_usuario ON academico_solicitacoes(unidade_id,usuario_id,id DESC);
CREATE TABLE academico_complementares (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL, aluno_id BIGINT NOT NULL,
 titulo VARCHAR(160) NOT NULL, data DATE NOT NULL, horas NUMERIC(7,2) NOT NULL CHECK(horas>0 AND horas<=1000),
 comprovante_url VARCHAR(2000), status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK(status IN ('PENDENTE','APROVADA','REPROVADA')),
 horas_aprovadas NUMERIC(7,2) NOT NULL DEFAULT 0 CHECK(horas_aprovadas>=0 AND horas_aprovadas<=horas),
 parecer VARCHAR(2000), avaliado_por BIGINT REFERENCES usuarios(id), avaliado_em TIMESTAMPTZ,
 criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 CHECK((status='APROVADA' AND horas_aprovadas>0) OR (status<>'APROVADA' AND horas_aprovadas=0)),
 FOREIGN KEY(aluno_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE INDEX ix_complementares_aluno ON academico_complementares(unidade_id,aluno_id,id DESC);
CREATE TABLE academico_horarios (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL, turma_id BIGINT NOT NULL,
 dia INT NOT NULL CHECK(dia BETWEEN 1 AND 7), inicio TIME NOT NULL, fim TIME NOT NULL,
 sala VARCHAR(100) NOT NULL DEFAULT '', ativo BOOLEAN NOT NULL DEFAULT true,
 criado_em TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(fim>inicio),
 FOREIGN KEY(turma_id,unidade_id) REFERENCES academico_turmas(id,unidade_id)
);
CREATE INDEX ix_horarios_turma ON academico_horarios(unidade_id,turma_id,dia,inicio);
