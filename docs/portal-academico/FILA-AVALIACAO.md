# Fila de avaliação — incremento assistido

Executor real: **LEM, com assistência IA**. Frentes designadas: Eduardo/backend (`fduduu`), Leônidas/frontend (`ldevloper`), Endeson/produto e integração (`endeson12`). Publicação por conta designada não significa programação ou revisão humana.

## Contrato e aceite

- `GET /api/academico/entregas?situacao=PENDENTE|RASCUNHO|PUBLICADA` combina o filtro com `atividadeId`, dentro da unidade e dos vínculos já autorizados. Sem filtro preserva a consulta anterior. Valores inválidos: 400.
- Equipe docente: pendente = sem nota e não publicada; rascunho = nota presente e não publicada; publicada = publicada. **Nota zero é avaliação válida**, não pendência.
- Aluno: pendente inclui toda avaliação não publicada, sem revelar nota/feedback nem existência de rascunho. Solicitar `RASCUNHO` explicitamente retorna 403.
- Tela Entregas oferece situações conforme perfil, mantém busca/atividade na URL, restaura após reload e volta à primeira página ao mudar situação. O filtro é aplicado no servidor antes do limite de 1.000; não é paginação ilimitada.
- Avaliar pela tela remove a entrega da fila anterior depois de salvar. Publicar torna a nota visível ao aluno e ao boletim, sem cálculo de média institucional.

## Evidências executadas

1. RED backend: novo teste `PortalIntegrationTest#filaDeAvaliacaoFiltraSemRevelarRascunhosAoAluno` falhou porque `PUBLICADA` retornava entrega pendente. Recibo privado `baseline-20261116T011751Z.json`.
2. GREEN e regressão: `mvn -B -ntp clean verify`, Java 21/Maven em imagem fixada pelo runner isolado existente, PostgreSQL privado `portal_baseline_test`: **31 testes, zero falhas/erros/ignorados**, recibo `baseline-20261116T012423Z.json`. Inclui isolamento de unidade/docente, validação e privacidade de rascunhos. Avisos existentes Mockito/Byte Buddy/CDS permanecem.
3. RED frontend: Chromium não encontrou o seletor inexistente. GREEN: `node scripts/evaluation_filter_ui_test.cjs`, **dois perfis**, ativos reais com API explicitamente simulada. Verifica filtros, URL, atividade, busca, reload e vazio. Não prova backend.
4. Integração real: `QA_BASE=http://127.0.0.1:18089 node scripts/evaluation_filter_e2e.cjs`, Chromium + JAR desta worktree + PostgreSQL privado. Cria apenas fixtures QA; autentica professor/aluno separadamente; entrega → pendente → salvar rascunho com nota zero pela UI → publicar pela UI → reload → aluno consulta entrega e boletim persistido. Este script recusa endereços não loopback. Uma tentativa inicial antecedeu a prontidão do servidor; outra expôs espera prematura do harness após salvar, corrigida esperando a remoção da linha.

Scripts Node requerem `playwright` disponível no `NODE_PATH` ou instalado localmente, e Chrome (`CHROME_PATH`, padrão `/usr/bin/google-chrome`). Nesta VPS: `NODE_PATH=/root/.hermes/workspaces/design-toolkit/runtime/node_modules`. Não instalar dependências globais. Credenciais demonstrativas dos testes destinam-se somente ao banco isolado, nunca produção.

Sem merge, deploy, alteração de infraestrutura, dados de pacientes ou aceite humano. PRs empilhadas devem ser revisadas na ordem backend → frontend → integração; revisão externa continua pendente. PRs concorrentes de segurança não são substituídas por esta entrega.
