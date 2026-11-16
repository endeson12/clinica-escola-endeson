# Fila acadêmica: 30 aceites incrementais

Executor real: **LEM, com assistência IA**. Publicação designada: `fduduu` (backend), `ldevloper` (frontend), `endeson12` (integração/QA). Contas publicadoras não significam programação ou revisão humana. Esta rodada acrescenta 10 commits independentes por frente após a PR #27; sem merge/deploy, fechamento de issues ou alteração das PRs anteriores.

## Escopo e evidências

- Backend: 10 contratos de comportamento existente, com asserts de autorização/privacidade, validação, persistência e ordenação, em MockMvc e PostgreSQL privado. Não são 10 correções de produção. Último `clean verify`: **41 testes, zero falhas/erros/ignorados**, recibo `baseline-20261116T014456Z.json`. A primeira fixture colidiu nome de turma: corrigida com UUID; não foi defeito de produção. Permanecem avisos Mockito/Byte Buddy/CDS.
- Frontend: 10 mudanças úteis e testes Chromium independentes, com RED antes da alteração e GREEN depois. API **explicitamente simulada** nesses testes de componente. Busca sem acento/espaços, URL canônica, remoção seletiva de filtros, vazio contextual, contagem anunciada, legenda de tabela, nomes acessíveis e explicação de privacidade. Sem biblioteca ou redesign de marca.
- Integração: 10 cenários contra JAR desta árvore + PostgreSQL privado, sem API simulada. Fixtures sintéticas, sessões independentes. Alguns cenários validam HTTP, outros também exercitam UI; criação de um navegador não transforma um teste HTTP em aceite visual. Inclui entrada hostil, CSRF, retirada/republicação, colegas, troca docente, cancelamento de matrícula, histórico, deep link privado, celular e validação do formulário.
- Regressões anteriores de filtro/UI e envio/publicação no navegador também executadas. Logs/resultados e manifesto com hashes completos são entregues junto à PR de integração.

## Executar

Pré-requisitos: Java 21/Maven/PostgreSQL descartável para `mvn -B -ntp clean verify`; `playwright` local e Chromium para Node. Não executar testes de integração contra produção/homologação ou encaminhamento de porta para banco real. O loopback é um guard adicional, não prova por si só isolamento do banco.

```sh
# Neste ambiente; pode ser substituído por dependência local equivalente.
export NODE_PATH=/root/.hermes/workspaces/design-toolkit/runtime/node_modules
node scripts/run_queue_checks.cjs --ui
# Somente após subir JAR + PostgreSQL privados, explicitamente em loopback:
QA_BASE=http://127.0.0.1:18089 node scripts/run_queue_checks.cjs --e2e
```

`CHROME_PATH` substitui `/usr/bin/google-chrome`. `QA_REPORT` grava relatório JSON fora do repositório, `QA_EVIDENCE_DIR` grava capturas sintéticas. O runner executa 11 scripts por modalidade: 10 novos e o script anterior. O script UI anterior cobre dois perfis. Testes falhando propagam exit code 1, sem pular cenários.

## Matriz de aceites

| Item | Frente | Objetivo independente | Teste |
|---|---|---|---|
| B01 | backend | normalizaSituacaoSemAlterarEscopo | `QueueNormalizedStatusTest#normalizaSituacaoSemAlterarEscopo` |
| B02 | backend | rejeitaSituacaoInvalidaSemMudarEntrega | `QueueInvalidStatusTest#rejeitaSituacaoInvalidaSemMudarEntrega` |
| B03 | backend | notaZeroPrivadaNaoVazaFeedback | `QueueDraftPrivacyTest#notaZeroPrivadaNaoVazaFeedback` |
| B04 | backend | limitesDeNotaPreservamUltimaAvaliacao | `QueueGradeBoundsTest#limitesDeNotaPreservamUltimaAvaliacao` |
| B05 | backend | feedbackNoLimiteERejeicaoSemTruncar | `QueueFeedbackBoundsTest#feedbackNoLimiteERejeicaoSemTruncar` |
| B06 | backend | entregaFinalNaoPodeSerDuplicada | `QueueDuplicateDeliveryTest#entregaFinalNaoPodeSerDuplicada` |
| B07 | backend | prazoEncerradoNaoGravaEntrega | `QueueDeadlineTest#prazoEncerradoNaoGravaEntrega` |
| B08 | backend | filaDaAtividadeOrdenaMaisRecentes | `QueueQueueOrderTest#filaDaAtividadeOrdenaMaisRecentes` |
| B09 | backend | atividadeAlheiaNaoPermiteEnumerarAvaliacoes | `QueueActivityBoundaryTest#atividadeAlheiaNaoPermiteEnumerarAvaliacoes` |
| B10 | backend | filaExigeSessaoEPerfilAcademico | `QueueQueueAuthenticationTest#filaExigeSessaoEPerfilAcademico` |
| F01 | frontend | Busca ignora acentos | `scripts/queue-ui/F01.cjs` |
| F02 | frontend | Busca tolera espaços nas extremidades | `scripts/queue-ui/F02.cjs` |
| F03 | frontend | Situação canônica reflete URL normalizada | `scripts/queue-ui/F03.cjs` |
| F04 | frontend | Limpar filtros mantém atividade | `scripts/queue-ui/F04.cjs` |
| F05 | frontend | Remover atividade mantém situação e busca | `scripts/queue-ui/F05.cjs` |
| F06 | frontend | Vazio de avaliação diferencia filtro de cadastro | `scripts/queue-ui/F06.cjs` |
| F07 | frontend | Contagem anunciada após buscar | `scripts/queue-ui/F07.cjs` |
| F08 | frontend | Tabela com legenda de contexto | `scripts/queue-ui/F08.cjs` |
| F09 | frontend | Ações da entrega têm nomes distintos | `scripts/queue-ui/F09.cjs` |
| F10 | frontend | Aviso explica privacidade do aluno | `scripts/queue-ui/F10.cjs` |
| Q01 | integração/QA | Conteúdo hostil permanece texto no navegador | `scripts/queue-e2e/Q01.cjs` |
| Q02 | integração/QA | CSRF inválido não grava avaliação | `scripts/queue-e2e/Q02.cjs` |
| Q03 | integração/QA | Retirada e republicação na mesma sessão do aluno | `scripts/queue-e2e/Q03.cjs` |
| Q04 | integração/QA | Colegas da mesma turma mantêm entregas separadas | `scripts/queue-e2e/Q04.cjs` |
| Q05 | integração/QA | Troca docente revoga fila sem invalidar sessão | `scripts/queue-e2e/Q05.cjs` |
| Q06 | integração/QA | Cancelamento de matrícula bloqueia leitura e novo envio | `scripts/queue-e2e/Q06.cjs` |
| Q07 | integração/QA | Navegador restaura filtros ao voltar e recarregar | `scripts/queue-e2e/Q07.cjs` |
| Q08 | integração/QA | Deep link de rascunho não revela dados ao aluno | `scripts/queue-e2e/Q08.cjs` |
| Q09 | integração/QA | Fila móvel mantém controles clicáveis e URL após reload | `scripts/queue-e2e/Q09.cjs` |
| Q10 | integração/QA | Formulário recusa nota inválida e salva zero como rascunho | `scripts/queue-e2e/Q10.cjs` |

## Critérios de UI adotados e limites

Preservados identidade, tokens e componentes existentes. Consultadas orientação Impeccable/polish (refinamento sem redesign) e cópia offline das Web Interface Guidelines (labels/semântica, estado na URL, anúncio assíncrono). Sem Figma, engine, novas imagens ou motion. Inspeção real em 390×844 e 1280×900. Tabela móvel mantém rolagem horizontal interna: o teste aciona “Ler” na última coluna sem rolagem horizontal da página. Ajuda/legendas ainda têm apresentação compacta; não foi certificada WCAG ou experiência em todos os aparelhos. Não há aceite humano.

A política existente de Actions só dispara PRs com base `main`. PRs empilhadas não recebem checks automaticamente; execução manual do workflow requer recibo real separado, não inferir CI verde de testes locais.
