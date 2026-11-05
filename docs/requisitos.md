# Requisitos e critérios de aceite

Fonte: `PRD.md` (PRD mestre de 05/11/2026). Resumo do escopo entregue:

## Funcionais

- **RF-01 Login:** unidade (Jóquei ou Redenção), e-mail e senha; validação de e-mail e campos vazios; mensagem para credenciais incorretas e para unidade não autorizada; mostrar/ocultar senha; estado "Entrando..."; redireciona para a área interna; logout; páginas internas bloqueadas sem sessão. Login por CPF/matrícula **não** foi implementado (decisão do PRD: login por e-mail).
- **RF-02 Pacientes:** nome, CPF e e-mail obrigatórios; telefone e nascimento opcionais; unidade vem da sessão; CPF com dígitos verificadores, sem sequências repetidas, normalizado e único por unidade; erros por campo; confirmação; tabela da unidade atual.
- **RF-03 Especialidades:** nome obrigatório e único, descrição opcional, situação ativa/inativa; só as ativas aparecem para novos atendimentos.
- **RF-04 Atendimentos:** paciente e especialidade vindos do banco; data/hora; status Agendado/Realizado/Cancelado; observações opcionais; unidade da sessão; paciente precisa ser da mesma unidade e a especialidade precisa estar ativa.
- **RF-05 Início:** unidade atual, atalhos para as três telas e contadores reais.

## Fora do escopo (não implementado)

Prontuário, prescrição, faturamento, integração com sistemas da UNINASSAU, login institucional, notificações, aplicativo, relatórios, microsserviços, API Gateway separado, edição e exclusão de registros.

## Matriz de testes

Resultados registrados em `CHECKLIST.md` e em `docs/evidencias/`.
