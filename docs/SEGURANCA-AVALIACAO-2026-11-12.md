# Revogação docente preserva avaliação publicada — 12/11/2026

Frente planejada: Eduardo/backend e segurança (#4), com impacto no fluxo acadêmico (#7). Executor REAL: LEM, assistência GPT-6.1 Sol/openai-codex. Não representa execução ou revisão humana dos colaboradores.

## Incremento delimitado

Complemento da PR #22: teste de comportamento existente, sem alterar produção. Professor publica nota/feedback; seu perfil acadêmico é alterado para ALUNO diretamente no PostgreSQL privado de teste, simulando revogação administrativa. A mesma sessão continua válida e já reflete ALUNO. Tentativa posterior de alterar nota, feedback e publicação recebe 403. As três propriedades permanecem no banco e o boletim do aluno continua exibindo a nota publicada original.

A revogação é preparada pela fixture SQL, não por fluxo administrativo de navegador. A revisão delimitada conferiu o gate `access.require` anterior à escrita em `PortalLearningService.avaliar`. Não foi demonstrada vulnerabilidade: o teste passou na implementação existente; não se alega correção TDD RED/GREEN. Nenhum código de produção foi escrito.

## Verificação local fresca

- `mvn -B -ntp clean verify`, Java 21, imagem Maven fixada pelo digest do runner baseline.
- 33 testes, zero falhas/erros/ignorados; seis relatórios Surefire e JAR produzido.
- Recibo: `baseline-20261112T120901Z.json`; banco exclusivo `portal_baseline_test`, backup após execução e contêiner parado pelo runner.
- Avisos existentes Mockito/Byte Buddy permanecem. Não foram alteradas versões.
- Worktree destacada no head anterior da PR #22, sem alterar árvores concorrentes.

## Gates e limites

Scanner redigido, diff-check, readback remoto e CI do novo head serão registrados na PR/issue #10 após execução. Este documento não antecipa esses resultados.

Sem merge, deploy, migração, dados reais, banco de homologação/produção, mudanças visuais ou cron. Não inclui os três testes de disponibilidade da PR #23. Revisão assistida da executora não substitui revisão externa nem aceite do PO. Issues #2/#4 permanecem em andamento; não certifica portal completo ou navegador ponta a ponta.
