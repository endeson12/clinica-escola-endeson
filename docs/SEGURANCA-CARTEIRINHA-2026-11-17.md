# Carteirinha demonstrativa — 17/11/2026

## Incremento e autoria

Frente planejada: Eduardo/backend e segurança (#4), carteirinha demonstrativa (#8). Executor REAL: LEM, assistência GPT-6.1 Sol/openai-codex. Publicação pela integração existente do titular, sem atribuir execução humana a colegas.

Um teste de integração adicional na PR #22 existente cobre dois alunos e duas matrículas do titular em cursos distintos. Mesma sessão: carteirinha contém exatamente os dois cursos; cancelar uma matrícula retira apenas o curso correspondente; cancelar a última retorna 404, apesar de sessão e perfil ALUNO continuarem válidos. Identificador permanece próprio e estável durante a transição. O colega mantém carteirinha e matrícula ativa; consultas cruzadas retornam 403 nas duas direções, e administrador de outra unidade recebe 404. PostgreSQL conserva as duas matrículas canceladas e a matrícula ativa do colega. Resposta inicial exige `oficial=false` e aviso SEM VALIDADE OFICIAL.

Cobertura do comportamento existente: passou sem mudança de produção. Não se alega correção de vulnerabilidade nem ciclo TDD RED/GREEN de comportamento novo. MockMvc utiliza filtros/serviços reais e PostgreSQL privado de teste; não comprova navegador, cache frontend, carteirinha oficial ou aceite integral do módulo.

## Verificação local executada

- `mvn -B -ntp clean verify`, Java 21/imagem Maven fixada no runner: 38 testes, zero falhas/erros/ignorados; JAR produzido.
- PostgreSQL exclusivo de teste; runner preservado, sem uso de homologação/produção.
- Gitleaks redigido em `src`: sem achados; `git diff --check` aprovado.
- Revisão delimitada pela própria executora: somente teste adicionado e este documento; sem mudança de API, migração, versão ou interface.
- Avisos existentes Mockito/Byte Buddy permanecem. CI do novo commit ainda precisa de conferência independente e artifact Surefire.

## Gates e limites

Revisão externa real/aceite do head validado continuam pendentes. Nenhum merge/deploy, alteração de cron ou dado real. Issues #2/#4 continuam em andamento; #8 não concluída. Trabalho concorrente e outras PRs preservados por worktree destacada própria. A meta de 20/11 não significa portal completo: requer demonstração ponta a ponta e aceite.
