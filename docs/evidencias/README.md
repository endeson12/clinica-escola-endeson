# Evidências

Ambiente: produção (https://clinica-escola-endeson.vercel.app), banco PostgreSQL Neon. Execução em 05/11/2026 com dados **fictícios**.

## Capturas de tela

| Arquivo | O que mostra |
|---|---|
| `00-login-desktop.png` | Tela de login (unidade, e-mail, senha, mostrar/ocultar) |
| `01-login-erro-credenciais.png` | Senha incorreta: mensagem genérica, sem autenticar |
| `02-dashboard.png` | Página inicial após login, unidade da sessão e contadores reais |
| `03-paciente-cpf-invalido.png` | CPF inválido rejeitado com erro no campo |
| `04-paciente-cadastrado.png` | Paciente cadastrado e listado (persistido no PostgreSQL) |
| `05-especialidades.png` | Especialidades cadastradas, ativas/inativas, duplicidade rejeitada |
| `06-atendimentos.png` | Atendimento registrado com paciente, especialidade, status e unidade |
| `07-celular-atendimentos.png` | Versão celular (390 px): tabela vira cartões, sem rolagem lateral |
| `08-celular-login-apos-logout.png` | Logout: sessão encerrada e volta ao login |

## Testes end-to-end na produção (API pública)

Resultado: **35/35 aprovados**.

| # | Teste | Resultado |
|---|---|---|
| 1 | Health check (app + banco) | Aprovado |
| 2 | Unidades seed (Jóquei e Redenção) | Aprovado |
| 3 | API interna sem sessão -> 401 | Aprovado |
| 4 | Página interna sem sessão -> redireciona ao login | Aprovado |
| 5 | Senha incorreta -> 401 com mensagem genérica | Aprovado |
| 6 | E-mail inválido -> 400 no campo email | Aprovado |
| 7 | Campos vazios -> 400 | Aprovado |
| 8 | Login sem token CSRF -> 403 | Aprovado |
| 9 | Unidade não autorizada -> 403 | Aprovado |
| 10 | Login correto -> 200 | Aprovado |
| 11 | Cookie de sessão HttpOnly + Secure + SameSite | Aprovado |
| 12 | Consulta da sessão (/api/auth/me) | Aprovado |
| 13 | Especialidades válidas persistidas | Aprovado |
| 14 | Especialidade duplicada -> 409 | Aprovado |
| 15 | Especialidade sem nome -> 400 | Aprovado |
| 16 | Especialidade inativa cadastrada | Aprovado |
| 17 | Inativa não aparece nas ativas | Aprovado |
| 18 | CPF inválido -> 400 | Aprovado |
| 19 | CPF sequência repetida -> 400 | Aprovado |
| 20 | Paciente válido persistido (CPF normalizado) | Aprovado |
| 21 | CPF duplicado na mesma unidade -> 409 | Aprovado |
| 22 | E-mail de paciente inválido -> 400 | Aprovado |
| 23 | Atendimento válido com relacionamentos | Aprovado |
| 24 | Atendimento com paciente inexistente -> 422 | Aprovado |
| 25 | Atendimento com especialidade inativa -> 422 | Aprovado |
| 26 | Atendimento sem data -> 400 | Aprovado |
| 27 | Atendimento com status inválido -> 400 | Aprovado |
| 28 | Outra unidade não vê pacientes do Jóquei | Aprovado |
| 29 | Outra unidade não vê atendimentos do Jóquei | Aprovado |
| 30 | Outra unidade não usa paciente do Jóquei -> 422 | Aprovado |
| 31 | Mesmo CPF permitido em outra unidade (regra por unidade) | Aprovado |
| 32 | Persistência: dados visíveis em nova sessão | Aprovado |
| 33 | Dashboard com contadores reais | Aprovado |
| 34 | Logout -> 204 | Aprovado |
| 35 | Após logout a sessão não vale mais -> 401 | Aprovado |

## Teste pela interface (navegador)

Feito na produção: validação de campos vazios e e-mail inválido no login, senha incorreta, mostrar/ocultar senha, dashboard, CPF inválido na tela, cadastro de paciente com máscara de CPF e telefone, persistência após recarregar a página, especialidade nova e duplicada, atendimento com campos vazios e atendimento válido, layout no celular e logout (página interna bloqueada depois).

## Conferência direta no banco (Neon)

Tabelas: `atendimentos, especialidades, flyway_schema_history, pacientes, spring_session, spring_session_attributes, unidades, usuarios, usuarios_unidades`.
Migrações aplicadas: `1 create tables | 2 seed unidades | 3 spring session`.
Senhas: 100% em hash BCrypt.
Restrições presentes: FKs de atendimentos (unidade, paciente, especialidade), FKs de pacientes e usuarios_unidades, `uk_pacientes_unidade_cpf`, `uk_usuarios_email`, `uk_unidades_codigo`, `ck_atendimentos_status`, `ck_pacientes_cpf_digitos`.

## Testes automatizados (Maven)

`./mvnw test`: 5 testes, 0 falhas (4 de validação de CPF e 1 de integração contra PostgreSQL real cobrindo login, unidade não autorizada, CPF inválido/duplicado, relacionamentos, isolamento por unidade e logout).
