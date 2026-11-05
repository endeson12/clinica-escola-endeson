-- V2: unidades universitarias atendidas pela Clinica-Escola
INSERT INTO unidades (nome, codigo) VALUES
    ('UNINASSAU Jóquei',   'JOQUEI'),
    ('UNINASSAU Redenção', 'REDENCAO')
ON CONFLICT (codigo) DO NOTHING;

-- Os usuarios de demonstracao NAO sao criados aqui.
-- Eles sao criados pela aplicacao na inicializacao (DemoUserInitializer),
-- com senha vinda de variavel de ambiente e gravada apenas como hash BCrypt.
