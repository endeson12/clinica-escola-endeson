# Banco de dados (PostgreSQL)

As tabelas são criadas pelas migrações Flyway em `src/main/resources/db/migration`:

| Arquivo | Conteúdo |
|---|---|
| `V1__create_tables.sql` | Tabelas, chaves primárias e estrangeiras, restrições e índices |
| `V2__seed_unidades.sql` | Cadastro das duas unidades (Jóquei e Redenção) |
| `V3__spring_session.sql` | Tabelas da sessão de login (Spring Session JDBC) |

O Flyway registra o que já foi aplicado na tabela `flyway_schema_history`. Por isso, rodar a aplicação de novo não recria nem apaga dados.

## Tabelas e relacionamentos

```
unidades 1 ──< usuarios_unidades >── 1 usuarios
unidades 1 ──< pacientes
unidades 1 ──< atendimentos >── 1 pacientes
                 atendimentos >── 1 especialidades
```

| Tabela | Campos | Regras |
|---|---|---|
| `unidades` | id, nome, codigo | `codigo` único |
| `usuarios` | id, nome, email, senha_hash, ativo, criado_em | `email` único; senha só como hash BCrypt |
| `usuarios_unidades` | usuario_id, unidade_id | PK composta; define quais unidades cada usuário pode acessar |
| `pacientes` | id, unidade_id, nome_completo, cpf, email, telefone, data_nascimento, criado_em | `UNIQUE (unidade_id, cpf)`; CPF com 11 dígitos (`CHECK`) |
| `especialidades` | id, nome, descricao, ativa, criado_em | nome único sem diferenciar maiúsculas (`UNIQUE INDEX lower(nome)`) |
| `atendimentos` | id, unidade_id, paciente_id, especialidade_id, data_hora, status, observacoes, criado_em | FKs reais; `status IN ('AGENDADO','REALIZADO','CANCELADO')` |

Todas as chaves estrangeiras usam `ON DELETE RESTRICT`, exceto `usuarios_unidades.usuario_id`, que é `CASCADE`. Assim nenhum atendimento fica apontando para paciente ou especialidade inexistente.

## Usuários de demonstração

Por segurança, os usuários **não** são criados por SQL com senha fixa. A classe `config/DemoUserInitializer` cria (ou atualiza) os usuários de forma idempotente ao iniciar, lendo as senhas das variáveis de ambiente e gravando apenas o hash BCrypt:

| Variáveis | Acesso |
|---|---|
| `DEMO_ADMIN_EMAIL` / `DEMO_ADMIN_PASSWORD` | Administrador, nas duas unidades |
| `DEMO_RECEPCAO_EMAIL` / `DEMO_RECEPCAO_PASSWORD` | Recepção, só na unidade Jóquei (demonstra "unidade não autorizada") |

## Reproduzir o banco do zero

1. Crie um banco PostgreSQL vazio.
2. Configure `JDBC_DATABASE_URL`, `DB_USERNAME` e `DB_PASSWORD` (ou `DATABASE_URL` no formato `postgres://`).
3. Inicie a aplicação: o Flyway aplica V1, V2 e V3 automaticamente.

Também é possível aplicar os arquivos `.sql` manualmente, na ordem, com `psql -f`.
