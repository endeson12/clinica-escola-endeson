# Regressão de vínculo revogado — 08/11/2026

Frente planejada: Eduardo/backend e segurança (#4). Execução real: LEM, assistência de IA GPT-6.1 Sol/openai-codex. Não representa implementação ou revisão humana de outro integrante.

## Incremento

Adicionar cobertura de integração para aluno cujo vínculo com a unidade foi removido antes de uma nova matrícula. O teste cria somente pessoas fictícias no PostgreSQL privado de teste e verifica:

- POST de matrícula responde 404;
- nenhuma matrícula desse aluno é persistida na turma;
- a sessão anterior do aluno recebe 401 ao consultar turmas.

A hipótese inicial de ausência de proteção foi refutada: a FK de `academico_perfis` para `usuarios_unidades` já usa `ON DELETE CASCADE`, e a busca da pessoa rejeita o registro sem perfil. O primeiro ensaio falhou somente porque esperava 400 em vez do 404 existente. Isso **não é evidência de vulnerabilidade nem ciclo RED/GREEN de correção**. O incremento é cobertura de comportamento existente; nenhum código de produção foi alterado.

## Verificação executada

- Ensaio direcionado: `mvn -B -ntp -Dtest=PortalIntegrationTest#alunoSemVinculoComUnidadeNaoRecebeNovaMatricula test`: 1 teste, 1 falha por status esperado incorreto (400 versus 404), 0 erros.
- Após alinhar a expectativa ao contrato existente: `mvn -B -ntp clean verify`: 31 testes, 0 falhas, 0 erros e 0 ignorados; JAR produzido.
- Java 21 e PostgreSQL isolado `portal_baseline_test`, imagem Maven fixada pelo digest do script de baseline. Script original preservado; somente caminho da worktree e argumentos Maven substituídos em memória. Banco de teste copiado em backup pelo próprio script.
- Gitleaks com `--no-git --redact`: nenhum achado. `git diff --check`: aprovado.
- Avisos existentes de Mockito/Byte Buddy sobre carregamento dinâmico de agente permanecem; não foram ocultados nem corrigidos neste incremento.
- Recibos privados: `baseline-20261108T120641Z.json` (ensaio) e `baseline-20261108T120734Z.json` (suite completa), no diretório de auditoria do projeto. Backups de fontes e relatório redigido de scanner em `daily-20261108/` fora do Git.

## Limites e próximo gate

A worktree original e trabalhos concorrentes foram preservados. Sem migração, mudança visual, deploy, alteração de cron ou acesso ao banco de homologação/produção. MockMvc com PostgreSQL não demonstra browser, HTTPS ou aceite ponta a ponta. A issue #4 permanece em andamento: este cenário não conclui toda a matriz de permissões e isolamento. Próximo gate: CI do mesmo SHA e revisão da PR; aceite de produto permanece com Endeson.
