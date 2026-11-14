# Visibilidade após retirada de publicação — 14/11/2026

## Escopo e autoria

Frente planejada: Eduardo/backend e segurança (#4), fluxo acadêmico (#7). Executor REAL: LEM, assistência autorizada GPT-6.1 Sol/openai-codex. Sem atribuir execução ou revisão humana aos colaboradores.

Um teste acrescentado à PR #22 existente: `retirarPublicacaoOcultaNotaNaMesmaSessaoSemApagarAvaliacao`.

## Contrato exercitado

1. Professor publica avaliação; aluno consulta nota/feedback nas entregas e no boletim.
2. Professor retira a publicação via API, mantendo nota e feedback.
3. A MESMA sessão do aluno consulta boletim vazio e entregas (com e sem filtro de atividade) sem nota/feedback, preservando o próprio texto entregue.
4. PostgreSQL mantém nota/feedback e `publicada=false`; professor continua consultando a avaliação privada.
5. Professor republica avaliação revisada; mesma sessão do aluno recebe a nova nota/feedback em entregas e boletim. Nota relida no PostgreSQL.

Cobertura do comportamento existente: nenhum código de produção foi alterado, nenhum defeito foi demonstrado e não se alega correção TDD RED/GREEN. MockMvc exercita os serviços reais e PostgreSQL privado, não navegador ou cache de frontend.

## Verificação local

- `mvn -B -ntp clean verify`, Java 21, Maven por digest do runner e PostgreSQL privado de teste: **35 testes, zero falhas/erros/ignorados**, JAR produzido.
- Recibo: `baseline-20261114T120756Z.json`, fora do repositório.
- Primeira tentativa atingiu o timeout do comando (240s), sem recibo completo; XMLs parciais não foram aceitos como evidência. Nova execução completa com limite 600s terminou com sucesso. Ocorrência preservada em `daily-20261114/first-attempt.json`.
- Avisos existentes Mockito/Byte Buddy/JVM permanecem; nenhuma versão foi alterada para ocultá-los.
- Totais não incluem os três testes separados da PR #23. CI deste novo head deve ser conferida separadamente na PR.

## Limites e próximo gate

Worktree própria, backup privado do teste anterior e push somente fast-forward. Sem migração, dados reais, banco de homologação/produção, alteração visual, merge, deploy ou cron. Revisão externa real e aceite permanecem pendentes. Issues #2/#4 não Done; a meta de 20/11 não comprova portal completo nem aceite ponta a ponta.
