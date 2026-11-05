# CHECKLIST — Clínica-Escola UNINASSAU

Legenda: ✅ Concluído e testado · 🔄 Em andamento · ⏳ Pendente · ⛔ Bloqueado

## Fase 0 — Inicialização e planejamento ✅
- ✅ PRD consolidado (`PRD.md`); login por e-mail (sem CPF/matrícula), conforme decisão do PRD
- ✅ Pasta independente `projeto rapido pro endeson`
- ✅ Acessos verificados: GitHub e Vercel
- ✅ Nomes livres: repositório e projeto `clinica-escola-endeson`
- Limitação: as capturas de referência do Endeson não chegaram nesta conversa; a paleta foi tirada do portal de referência (azul #2672EC + branco + neutros), sem afirmar que são cores oficiais

## Fase 1 — Spring Boot e banco ✅
- ✅ Maven + Java 21 + Spring Boot 4.1.2 (Web, Security, Data JPA, Validation, Flyway, Session JDBC)
- ✅ Entidades, repositories, migrações V1/V2/V3, health check
- ✅ Teste: aplicação sobe, migra e responde `/api/health` com PostgreSQL local e com Neon

## Fase 2 — Hospedagem antecipada ✅
- ✅ `Dockerfile.vercel` (JDK 21 no build, JRE 21 na execução, porta via `PORT`)
- ✅ Projeto Vercel `clinica-escola-endeson`, preset **Container**
- ✅ Neon criado pelo Vercel Marketplace e conectado ao projeto
- ✅ Health por HTTPS: `{"aplicacao":"UP","banco":"UP","unidadesCadastradas":2,"migracaoAtual":"3"}`
- Observação: o 1º deploy saiu como estático porque o projeto foi criado com preset "Other"; corrigido com `vercel project update --framework container`

## Fase 3 — Autenticação ✅
- ✅ Spring Security, BCrypt, sessão no PostgreSQL, cookie HttpOnly/Secure/SameSite, CSRF, troca de ID de sessão
- ✅ Usuários demo idempotentes por variável de ambiente (admin: 2 unidades; recepção: só Jóquei)
- ✅ Testes: login ok, senha errada, e-mail inválido, unidade não autorizada, sem sessão, logout

## Fase 4 — APIs de cadastro ✅
- ✅ 4.1 Especialidades (nome único sem diferenciar maiúsculas, ativa/inativa)
- ✅ 4.2 Pacientes (CPF com DV, normalizado, único por unidade, isolamento por unidade)
- ✅ 4.3 Atendimentos (paciente da unidade, especialidade ativa, status controlado, data obrigatória)

## Fase 5 — Frontend e integração ✅
- ✅ Login, dashboard, pacientes, especialidades, atendimentos (HTML/CSS/JS nativo, mesma origem)
- ✅ Erros por campo, toasts de sucesso/falha, estados de carregamento, tabelas responsivas
- ✅ Teste na produção pelo navegador (desktop 1366 px e celular 390 px)

## Fase 6 — GitHub ✅
- ✅ Repositório privado https://github.com/endeson12/clinica-escola-endeson
- ✅ Varredura: nenhum segredo, `.env`, `.vercel` ou `target` versionado

## Fase 7 — Deploy completo e validação ✅
- ✅ 35/35 testes end-to-end na produção
- ✅ Registros conferidos direto no Neon

## Fase 8 — Documentação e evidências ✅
- ✅ README, guia IntelliJ, banco de dados, requisitos, evidências com capturas

## Pendências e decisões
- Repositório está **privado**; tornar público ou adicionar o professor/Endeson como colaborador depende de autorização.
- Visual baseado no portal de referência; ajustar se o Endeson mandar as capturas.
- Edição/exclusão de registros ficou fora (P2 do PRD).
