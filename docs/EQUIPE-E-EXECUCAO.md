# Equipe e execução incremental

Plano aprovado pelo PO em 06/11/2026, mensagem Discord 1568368824573755444. Referência funcional: [proposta aprovada](portal-academico/PROPOSTA-2026-11-06.md).

## Responsabilidades planejadas

| Pessoa | Papel e frente | Conta GitHub | Situação da participação |
|---|---|---|---|
| Endeson Marcell | PO/Scrum Master: requisitos, prioridades, backlog, critérios de aceite e apresentação | `endeson12` | Conta do titular confirmada pelo repositório; decisões recebidas no canal do projeto |
| Eduardo | Desenvolvimento: backend Java, API, modelagem de dados, migrações e regras de negócio | `fduduu` — confirmado pelo PO | Frente designada; issues #2/#4/#7/#8 atribuídas à conta. Autoria ou execução só serão registradas quando acontecerem |
| Leônidas | Desenvolvimento: frontend, responsividade, acessibilidade, usabilidade e integração das telas | `ldevloper` — confirmado pelo PO | Acesso de escrita confirmado e issues #6/#7/#8 atribuídas; aceite do convite feito por automação autorizada. Não implica execução humana de código |
| LEM | Assistência de IA: implementação assistida, integração, automação de testes e relatos verificáveis | Não representa uma conta pessoal de integrante | Executora dos incrementos assistidos identificados nas PRs e atualizações |

Associação confirmada pelo PO na mensagem Discord **1568446040775065691**: **Eduardo = `fduduu`; Leônidas = `ldevloper`**. Em 07/11, ambas as contas tiveram identidade e escrita verificadas. O convite de Leônidas foi aceito por automação autorizada e as atribuições #6/#7/#8 foram relidas. Convite e assignee não comprovam execução humana. A equipe foi descrita pelo titular como profissional, desenvolvendo para uma instituição; o domínio educacional do produto não transforma a execução em exercício escolar.

## Contas designadas e automação autorizada

Autorização posterior do titular: mensagem Discord **1568683698180849827** e confirmação de propriedade das contas. Usar a conta designada para push/PR e atualização da respectiva frente: backend/dados com `fduduu`; frontend/usabilidade com `ldevloper`; decisões de produto/integração com `endeson12`. Não dividir commits artificialmente para inflar participação.

- Credenciais disponíveis apenas na integração privada da VPS, fora deste repositório, em `~/.hermes/secrets/github-clinica-equipe.json` (permissão 0600). Não copiar o arquivo, imprimir seu conteúdo, gravar em remote Git ou executar login global do `gh`.
- Ler somente a conta necessária em memória; passar `GH_TOKEN` apenas ao subprocesso da operação e restringir endpoints/remoto a `endeson12/clinica-escola-endeson`. Validar `/user` e permissão de escrita antes de publicar. Falha exige bloqueio explícito; não trocar silenciosamente de identidade.
- Chaves foram expostas originalmente no chat: o titular autorizou seu uso ciente do risco; substituição por credenciais restritas segue recomendada. Não expor tokens em logs, código, PR ou comentário.
- Código executado por IA deve ter autoria LEM e identificação explícita de automação no commit/PR. A conta que envia a PR identifica o responsável operacional, não prova que a pessoa escreveu manualmente.
- Não produzir aprovações fictícias entre contas, simular revisão humana, alterar datas ou acessar outros repositórios/serviços. Preservar trabalhos/PRs concorrentes.


## Divisão inicial do backlog

- PO: #3 (decisão/aceite) e #5 (integração da equipe).
- Backend/dados, Eduardo como responsável de frente planejado: #2, #4 e #7.
- Frontend/usabilidade, Leônidas como responsável de frente planejado: #6.
- Financeiro/carteirinha, Eduardo no backend e Leônidas na interface: #8.
- Disponibilidade/integração e execução assistida, LEM: #1 e tarefas de baseline.

Essa divisão NÃO transfere autoria de commits. O campo de responsável é separado do executor real de cada entrega. As identidades e as atribuições estão confirmadas; futuras mudanças de integrantes exigem confirmação do titular, não inferência.

## Fluxo de entrega

1. Ler issue e critérios de aceite; escolher um incremento delimitado.
2. Confirmar que nenhum trabalho concorrente será sobrescrito.
3. Escrever teste para comportamento novo/corrigido, observar falha e implementar o mínimo necessário.
4. Executar testes pertinentes e regressões em banco isolado; nunca usar dados reais da instituição.
5. Commit com autoria verdadeira; PR com resumo, testes, riscos e limitações.
6. Atualizar o quadro e o registro diário com links verificáveis.
7. Marcar Done somente após satisfazer critérios e conferir a evidência.

Não criar commits vazios, dividir alterações artificialmente ou alterar datas para aparentar atividade. A aprovação do plano não autoriza afirmar que integrantes executaram tarefas que a IA realizou.

## Formato da atualização diária

- **Data e incremento**: mudança concreta ou bloqueio; sem métricas de contribuição inventadas.
- **Responsável planejado**: frente de produto/backend/frontend/integração.
- **Executor real e assistência**: quem efetivamente fez o trabalho.
- **Entrega e evidências**: issue, PR, revisão e testes executados.
- **Estado**: proposto, em andamento, bloqueado, aguardando revisão ou concluído.
- **Próximo gate**: condição concreta para avançar, sem promessas não verificadas.

Um único registro por data deve ser atualizado ou complementado, sem comentários repetitivos em todas as issues. Havendo trabalho real de outro colaborador, preservá-lo e reconhecer a evidência correspondente. Não usar presença de um nome ou assignee como prova de contribuição.

## Limites

Sem cobrança real, carteirinha oficial, integração com credenciais institucionais, coleta de dados de pacientes ou contratação de serviços. Alterações destrutivas exigem backup e revisão. Trabalho diário por branches e PRs; não promover automaticamente migrações sensíveis ou deploys não validados.

A agenda e seu identificador serão registrados em `docs/OPERACAO-DIARIA.md` após confirmação do agendamento. Documentação histórica de baseline não equivale ao estado atual: consultar a última evidência antes de cada execução.
