# Operação diária — janela até 20/11/2026

Autorização do PO: mensagem Discord 1568368824573755444, de 06/11/2026. Escopo: proposta aprovada e responsabilidades em `EQUIPE-E-EXECUCAO.md`.

## Agenda confirmada

- Identificador: `5b74f499e426`.
- Estado no cadastro: **agendado**, ainda sem execução automática concluída em 06/11.
- Horário: **09h America/Fortaleza**, correspondente a 12h UTC.
- Primeira execução prevista: **07/11/2026, 09h**.
- Limite: **14 execuções**, com guarda adicional de data até **20/11/2026 inclusive**. A meta não é garantia de completude funcional.
- Modelo fixado: `gpt-6.1-sol`, provedor `openai-codex`.
- Destino dos resumos automáticos: Discord **#Criativos**; detalhes técnicos públicos e não sensíveis na [issue #10](https://github.com/endeson12/clinica-escola-endeson/issues/10).
- Escopo por rodada: um incremento verificável, limitado ao orçamento de tempo/ferramentas definido no job. Se bloqueado, relatar a causa em vez de inventar avanço.

O agendamento não comprova execução, testes, publicação ou recebimento futuro. O estado efetivo deve ser conferido no recibo do job e no registro de cada dia. Não há promessa de que todos os módulos estarão completos sem passar pelos critérios de aceite.

## Sequência de cada rodada

1. Conferir estado local/remoto, PRs, backlog e trabalhos concorrentes.
2. Selecionar uma entrega delimitada pela dependência e prioridade.
3. Para mudanças de comportamento: testar falha, implementar e testar regressões.
4. Usar somente PostgreSQL de teste isolado, sem dados acadêmicos/pacientes reais.
5. Abrir/atualizar PR com autoria verdadeira, testes e limitações.
6. Atualizar campos do Kanban distinguindo responsável planejado e executor real.
7. Publicar ou atualizar o registro diário com marcador `<!-- portal-daily:AAAA-MM-DD -->` para evitar duplicação.
8. Relê o registro e verifica os links antes de resumir a entrega no Discord.

Sem tokens expostos, coautoria fictícia, commits vazios ou alteração de datas. Integrações sensíveis/produção e migrações destrutivas não são automáticas. Falha de entrega de mensagem não é falha do aplicativo; são diagnósticos distintos.

## Baseline e continuidade

O primeiro build/teste real ocorreu manualmente em 06/11; não foi uma execução do job. Veja [BASELINE-2026-11-06.md](BASELINE-2026-11-06.md). A disponibilidade remota e os módulos novos continuam sujeitos a suas próprias verificações. Um build aprovado não comprova que a produção foi recuperada.
