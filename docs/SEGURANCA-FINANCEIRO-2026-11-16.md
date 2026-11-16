# Baixa demonstrativa: autorização e isolamento — 16/11/2026

## Incremento

Cobertura adicional do comportamento existente na PR #22, sem alteração de produção, migrações ou versões. Não se alega vulnerabilidade corrigida nem ciclo RED/GREEN: o teste exercita um contrato já implementado.

Teste: `PortalIntegrationTest#baixaDemonstrativaExigePerfilEUnidadeSemAlterarExtratoOuAuditoriaNasNegativas`.

- Criar dois lançamentos fictícios pela sessão FINANCEIRO, para dois alunos distintos.
- Reutilizar as sessões: aluno titular, colega e professor recebem 403 ao tentar baixar lançamento; administrador de outra unidade recebe 404.
- Após as negativas, conferir estado e quantidade de auditorias intactos no PostgreSQL isolado. Consultar extrato do colega por ID também retorna 403.
- Baixa autorizada pelo FINANCEIRO altera somente o lançamento alvo. O aluno recebe a situação atualizada e a marca demonstrativa na mesma sessão; o colega mantém somente seu próprio lançamento e estado anterior.
- Reler persistência e auditoria: exatamente uma baixa, com ator e unidade corretos; nenhuma auditoria adicional das tentativas negadas.

## Verificação e limites

Comando de validação: `mvn -B -ntp clean verify`, Java 21 e PostgreSQL privado pelo runner de baseline revisado, Maven fixado por digest. Resultado e SHA são registrados na PR e no comentário diário somente após execução concluída. Gitleaks com saída redigida e revisão do diff são gates anteriores ao commit.

MockMvc usa filtros, serviços e PostgreSQL reais; não comprova interface de navegador, cache frontend, operação bancária, homologação ou aceite do módulo inteiro. Dados fictícios e financeiro estritamente demonstrativo. Sem deploy ou acesso a produção. A carteira não foi alterada.

Frente planejada: Eduardo/backend e segurança; Leônidas/interface permanece uma responsabilidade distinta, sem alteração visual nesta rodada. Executor real: LEM, assistência GPT-6.1 Sol/openai-codex. Nenhuma execução humana ou coautoria inferida. Próximo gate: revisão externa/aceite do head validado antes de integrar. Issues #2/#4 permanecem em andamento; #8 não é declarada concluída.
