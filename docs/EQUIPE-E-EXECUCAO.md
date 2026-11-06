# Equipe e execução incremental

Plano aprovado pelo PO em 06/11/2026, mensagem Discord 1568368824573755444. Referência funcional: [proposta aprovada](portal-academico/PROPOSTA-2026-11-06.md).

## Responsabilidades planejadas

| Pessoa | Papel e frente | Conta GitHub | Situação da participação |
|---|---|---|---|
| Endeson Marcell | PO/Scrum Master: requisitos, prioridades, backlog, critérios de aceite e apresentação | `endeson12` | Conta do titular confirmada pelo repositório; decisões recebidas no canal do projeto |
| Eduardo | Desenvolvimento: backend Java, API, modelagem de dados, migrações e regras de negócio | A confirmar | Frente designada pelo PO; autoria ou execução só serão registradas quando acontecerem |
| Leônidas | Desenvolvimento: frontend, responsividade, acessibilidade, usabilidade e integração das telas | A confirmar | Frente designada pelo PO; autoria ou execução só serão registradas quando acontecerem |
| LEM | Assistência de IA: implementação assistida, integração, automação de testes e relatos verificáveis | Não representa uma conta pessoal de integrante | Executora dos incrementos assistidos identificados nas PRs e atualizações |

`fduduu` está cadastrado como colaborador com escrita; `ldevloper` tem convite de escrita pendente na inspeção de 06/11. A associação desses logins aos dois integrantes **ainda não foi confirmada**. Não usar tokens para descobrir identidades ou publicar como outra pessoa. Convite pendente não comprova participação ou aceite.

## Divisão inicial do backlog

- PO: #3 (decisão/aceite) e #5 (integração da equipe).
- Backend/dados, Eduardo como responsável de frente planejado: #2, #4 e #7.
- Frontend/usabilidade, Leônidas como responsável de frente planejado: #6.
- Financeiro/carteirinha, Eduardo no backend e Leônidas na interface: #8.
- Disponibilidade/integração e execução assistida, LEM: #1 e tarefas de baseline.

Essa divisão NÃO transfere autoria de commits. O campo de responsável planejado é separado do assignee confirmado e do executor real de cada entrega. Até os logins serem confirmados, não atribuir tarefas às contas de terceiros por inferência.

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
