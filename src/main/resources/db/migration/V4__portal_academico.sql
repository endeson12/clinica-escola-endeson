-- Portal independente: permissões por unidade, sem reaproveitar pacientes como alunos.
CREATE TABLE academico_perfis (
 usuario_id BIGINT NOT NULL, unidade_id BIGINT NOT NULL,
 perfil VARCHAR(20) NOT NULL CHECK(perfil IN ('ADMIN','COORDENACAO','PROFESSOR','ALUNO','FINANCEIRO','RECEPCAO')),
 PRIMARY KEY(usuario_id,unidade_id),
 FOREIGN KEY(usuario_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id) ON DELETE CASCADE
);
CREATE TABLE academico_cursos (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id),
 codigo VARCHAR(30) NOT NULL, nome VARCHAR(160) NOT NULL,
 carga_horaria INT NOT NULL CHECK(carga_horaria BETWEEN 1 AND 20000), ativo BOOLEAN NOT NULL DEFAULT TRUE,
 criado_em TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(id,unidade_id), UNIQUE(unidade_id,codigo)
);
CREATE TABLE academico_disciplinas (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), curso_id BIGINT NOT NULL,
 codigo VARCHAR(30) NOT NULL, nome VARCHAR(160) NOT NULL,
 carga_horaria INT NOT NULL CHECK(carga_horaria BETWEEN 1 AND 20000),
 UNIQUE(id,unidade_id), UNIQUE(unidade_id,curso_id,codigo),
 FOREIGN KEY(curso_id,unidade_id) REFERENCES academico_cursos(id,unidade_id)
);
CREATE TABLE academico_turmas (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), disciplina_id BIGINT NOT NULL,
 professor_id BIGINT NOT NULL, nome VARCHAR(160) NOT NULL, periodo VARCHAR(30) NOT NULL,
 vagas INT NOT NULL CHECK(vagas BETWEEN 1 AND 1000), criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(id,unidade_id), UNIQUE(unidade_id,nome,periodo),
 FOREIGN KEY(disciplina_id,unidade_id) REFERENCES academico_disciplinas(id,unidade_id),
 FOREIGN KEY(professor_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE TABLE academico_matriculas (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), turma_id BIGINT NOT NULL, aluno_id BIGINT NOT NULL,
 status VARCHAR(15) NOT NULL DEFAULT 'ATIVA' CHECK(status IN ('ATIVA','CANCELADA')),
 criado_em TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(turma_id,aluno_id),
 FOREIGN KEY(turma_id,unidade_id) REFERENCES academico_turmas(id,unidade_id),
 FOREIGN KEY(aluno_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE TABLE academico_aulas (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), turma_id BIGINT NOT NULL,
 titulo VARCHAR(160) NOT NULL, conteudo TEXT NOT NULL CHECK(length(conteudo) BETWEEN 1 AND 12000),
 material_url VARCHAR(2000), criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(turma_id,unidade_id) REFERENCES academico_turmas(id,unidade_id)
);
CREATE TABLE academico_atividades (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), turma_id BIGINT NOT NULL,
 titulo VARCHAR(160) NOT NULL, instrucoes TEXT NOT NULL CHECK(length(instrucoes) BETWEEN 1 AND 12000),
 prazo TIMESTAMPTZ NOT NULL, criado_em TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(id,unidade_id),
 FOREIGN KEY(turma_id,unidade_id) REFERENCES academico_turmas(id,unidade_id)
);
CREATE TABLE academico_entregas (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), atividade_id BIGINT NOT NULL, aluno_id BIGINT NOT NULL,
 conteudo TEXT NOT NULL CHECK(length(conteudo) BETWEEN 1 AND 12000), enviado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 nota NUMERIC(4,2) CHECK(nota BETWEEN 0 AND 10), feedback VARCHAR(4000), publicada BOOLEAN NOT NULL DEFAULT FALSE,
 avaliado_por BIGINT REFERENCES usuarios(id), avaliado_em TIMESTAMPTZ,
 CHECK(NOT publicada OR nota IS NOT NULL), UNIQUE(atividade_id,aluno_id),
 FOREIGN KEY(atividade_id,unidade_id) REFERENCES academico_atividades(id,unidade_id),
 FOREIGN KEY(aluno_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE TABLE academico_financeiro (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), aluno_id BIGINT NOT NULL,
 descricao VARCHAR(240) NOT NULL, valor NUMERIC(12,2) NOT NULL CHECK(valor>0), vencimento DATE NOT NULL,
 status VARCHAR(25) NOT NULL DEFAULT 'ABERTO' CHECK(status IN ('ABERTO','BAIXA_DEMONSTRATIVA')),
 demonstrativo BOOLEAN NOT NULL DEFAULT TRUE CHECK(demonstrativo), criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
 FOREIGN KEY(aluno_id,unidade_id) REFERENCES usuarios_unidades(usuario_id,unidade_id)
);
CREATE TABLE academico_auditoria (
 id BIGSERIAL PRIMARY KEY, unidade_id BIGINT NOT NULL REFERENCES unidades(id), ator_id BIGINT NOT NULL REFERENCES usuarios(id),
 entidade VARCHAR(30) NOT NULL, registro_id BIGINT NOT NULL, acao VARCHAR(40) NOT NULL, criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_matriculas_aluno_unidade ON academico_matriculas(unidade_id,aluno_id,status);
CREATE INDEX ix_turmas_professor_unidade ON academico_turmas(unidade_id,professor_id);
CREATE INDEX ix_aulas_turma ON academico_aulas(unidade_id,turma_id);
CREATE INDEX ix_atividades_turma ON academico_atividades(unidade_id,turma_id);
CREATE INDEX ix_entregas_aluno ON academico_entregas(unidade_id,aluno_id);
CREATE INDEX ix_financeiro_aluno ON academico_financeiro(unidade_id,aluno_id);
CREATE INDEX ix_auditoria_unidade ON academico_auditoria(unidade_id,id DESC);
