# Disponibilidade pública — verificação de 10/11/2026

## Incremento e autoria

Frente planejada: integração/disponibilidade — LEM (#1); backend/testes — Eduardo (#2). Executor REAL: **LEM**, assistência GPT-6.1 Sol/openai-codex. Não é contribuição manual ou revisão humana atribuída aos colegas.

Incremento restrito a três testes de integração e este registro. Nenhum código de produção, versão, migração, configuração de hospedagem ou cron alterado. A PR #22 continua separada, aberta e aguardando revisão externa; sua cobertura não está incluída nesta branch, baseada na main `ba94228`.

## Cobertura adicionada

- Health sem autenticação responde JSON com aplicação/banco UP, unidades existentes, migração não vazia e somente os quatro campos previstos.
- Página pública de login entrega HTML com campo de senha e recurso de autenticação.
- A disponibilidade do health não libera pacientes/atendimentos sem sessão: ambos permanecem HTTP 401.

São testes do **comportamento existente**, não correção de vulnerabilidade nem alteração funcional. Não se alega ciclo RED/GREEN de uma correção inexistente.

## Execução local

`mvn -B -ntp clean verify` em Java 21, Maven fixado por digest e PostgreSQL privado exclusivo de testes: **33 testes, zero falhas, erros ou ignorados**, JAR gerado. Recibo privado: `baseline-20261110T120509Z.json`. Avisos existentes Mockito/Byte Buddy permanecem. Banco de teste copiado por pg_dump e contêiner de baseline parado pelo runner; nenhum banco de homologação/produção usado pelos testes.

## Readback HTTPS real — 10/11, 09:06 America/Fortaleza

Somente GET, TLS verificado, sem credenciais, sem persistência de cookies ou gravação de dados de negócio. A própria aplicação pode gerar logs/cookie CSRF normalmente.

Base atual: https://clinica-escola.76.13.234.134.sslip.io

| Caminho | HTTP | Evidência |
|---|---|---|
| `/index.html` | 200 | HTML byte-idêntico ao arquivo da main inspecionada |
| `/js/auth.js` | 200 | JavaScript byte-idêntico ao arquivo da main inspecionada |
| `/api/health` | 200 | aplicação e banco UP; somente as chaves previstas |
| `/api/pacientes` | 401 | acesso anônimo negado |
| `/api/atendimentos` | 401 | acesso anônimo negado |

Base legada: https://clinica-escola-endeson.vercel.app — `/` e `/api/health` continuam **404**. Esta observação não identifica o deployment nem a causa raiz do 404; nenhuma configuração Vercel foi alterada.

## Limitações e próximo gate

O smoke HTTP confirma disponibilidade naquele instante e apenas os recursos consultados. Não comprova login autenticado, navegador, disponibilidade futura, todo o JAR remoto, restauração de produção, licença das imagens ou aceite ponta a ponta do portal. Os dois arquivos públicos iguais não provam qual commit completo está implantado.

Issue #1 permanece em andamento: demonstração atual acessível, investigação do deployment legado e critérios integrais pendentes. Próximo gate desta PR: CI do SHA publicado, revisão externa e aceite. Não há novo deploy nem merge nesta rodada.
