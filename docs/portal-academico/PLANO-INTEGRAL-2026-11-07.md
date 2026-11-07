# Plano de implementação integral — portal institucional independente

## Autorização e arquitetura
Escopo/desenho aprovado em `PROPOSTA-2026-11-06.md`, reiterado pelo titular em Discord1568688944483139775 (construir todo o sistema). Execução direta por LEM; backend publicado por fduduu, frontend por ldevloper, sem autoria/revisão humana fictícia. Monólito Java21/Spring/PostgreSQL, HTML/CSS/JS mesma origem. Sem troca de framework, recursos pagos ou dados institucionais reais.

## Contrato desta versão
- Perfis por unidade: ADMIN, COORDENACAO, PROFESSOR, ALUNO, FINANCEIRO, RECEPCAO. Sem perfil acadêmico explícito, o usuário continua restrito à clínica. Não converter pacientes em estudantes.
- Administração cria usuários, cursos, disciplinas, turmas e matrículas; matrícula duplicada/conflitante rejeitada. Professor deve estar vinculado à turma e aluno com matrícula ativa.
- Professor publica aulas/texto/material HTTPS, atividades com prazo, recebe entregas e registra avaliações. Aluno só consulta suas turmas, envia sua entrega e lê notas publicadas. Sem média, aprovação ou equivalência institucional automática. Escala explícita de cada avaliação:0–10, registro individual demonstrativo; sem boletim oficial.
- Financeiro registra lançamento fictício e baixa DEMONSTRATIVA; aluno só consulta seu extrato. Sem banco/Pix/boletos reais. Carteirinha própria somente com matrícula ativa, sem validade oficial.
- Clínica existente permanece disponível exclusivamente aos perfis ADMIN/RECEPCAO; nenhum papel acadêmico ganha acesso clínico implicitamente. Sessão revogada bloqueia APIs. Migrações novas/aditivas, constraints e transações.
- Auditoria registra ator/unidade/operação/identificador, sem senha/corpo clínico. Indicadores derivados de registros persistidos.

## Etapas e gates
1. Regressão de CSRF existente: integrar PR13 após CI, preservar os dois worktrees anteriores. Criar backup Git antes da expansão.
2. Escrever `PortalIntegrationTest.java` contra a API ainda ausente e executar RED em PostgreSQL isolado. Casos incluem fluxo completo, IDOR/unidade, perfil financeiro, professor alheio, matrícula/prazo/nota/URL inválidos.
3. Criar migraçãoV4 e arquivos `portal/{PortalModels,PortalAccess,PortalRepository,PortalCatalogService,PortalLearningService,PortalStudentService,PortalController}.java`. Completar GREEN sem alterar migrações V1–V3. Relatar contagens reais.
4. Publicar backend via conta Eduardo em PR; conferir conta/head/arquivos/CI. Não encerrar issues amplas sem critérios completos.
5. Criar `portal.html`, `css/portal.css`, `js/portal.js`; adaptar entrada/login e link para clínica. Referência travada na aplicação existente/proposta: navy/azul/branco, navegação operacional lateral, tabelas, formulários rotulados, estados vazios/erro/sucesso, foco e menu mobile. Sem marketing hero/contadores fabricados. Refero craft forms/focus + fundamentos Emil/Impeccable como refinamento, sem Refero/Figma conectado fictício e sem novas imagens desnecessárias.
6. Teste de navegador com três sessões independentes: admin cadastra→professor publica→aluno entrega→professor avalia/publica→aluno consulta nota/extrato/carteira. Recarregar/reentrar para persistência; negar IDs alheios; teclado/390px/1440px/console/rede e capturas.
7. Publicar frontend pela conta Leônidas em PR, CI e readback. Empacotar execução Java/Postgres, testar reinício/health/login. Deploy somente no projeto próprio e após capacidade/rota/HTTPS comprovadas; URL inexistente não é entrega.

## Limites explícitos
Não equivale a ERP universitário comercial. Sem SSO institucional, importações oficiais, vídeo próprio, uploads binários, cobrança real, certificados ou documentos oficiais. Regras de médias/recuperação não inventadas. Objetivo é uma versão integrada dos módulos aprovados, comprovada em execução e com limitações registradas.
