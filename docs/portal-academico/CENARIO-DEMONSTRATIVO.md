# Cenário demonstrativo preenchido

Carga aditiva autorizada para as unidades Jóquei e Redenção, executada em PostgreSQL em transação com ensaio/rollback e backup privado previamente validado. Os registros preexistentes foram comparados por digest em 19 tabelas e preservados.

## Conteúdo inicial inserido

- 72 pessoas fictícias: 48 alunos, 16 professores e 8 profissionais administrativos.
- 8 cursos, 24 disciplinas, 32 turmas (24 atuais e 8 históricas), 160 matrículas e 24 horários semanais.
- 72 aulas com conteúdo de estudo e 72 atividades; 240 entregas, incluindo notas publicadas, rascunhos e correções pendentes.
- 192 lançamentos estritamente demonstrativos, com itens abertos e baixas simuladas.
- 106 solicitações e 144 atividades complementares em situações diferentes.
- 48 pacientes fictícios, 6 especialidades demonstrativas e 96 atendimentos simulados.
- Dados pessoais fictícios preenchidos nas novas contas; o perfil pessoal do proprietário não foi substituído.

Cursos: Enfermagem, Fisioterapia, Psicologia e Nutrição. Períodos: 2026.2 e histórico 2026.1. Conteúdos e estados permanecem no banco, não no navegador. Contagens acima correspondem à carga inicial; navegação e testes funcionais podem gerar novos registros fictícios auditados.

## Proteções

- Nomes e conteúdos identificados como DEMO; e-mails em domínio reservado `.invalid`, telefones sem contato e CPFs propositalmente inválidos para impedir uso como identificação real.
- Contas novas possuem senhas aleatórias independentes, mantidas somente em arquivo privado fora do Git. Não há senha padrão compartilhada.
- Nenhum pagamento, mensagem externa, emissão de documento oficial ou atendimento real foi realizado.
- Material local é texto de demonstração; o link opcional abre o portal público do Ministério da Saúde, sem alegar autoria ou integração institucional.
- Não repetir a carga: a proteção por domínio e recibo rejeita duplicações.
- O SQL da carga, credenciais, manifestos com IDs e backup permanecem privados; este documento não contém dados de acesso.

## Correções reveladas pelo cenário mais completo

1. Priorizar períodos acadêmicos numéricos no seletor de Meu curso, sem apagar períodos antigos de homologação.
2. Incluir `turma_id` no boletim para vincular avaliações publicadas aos cartões e modais da disciplina. O filtro de publicação e as permissões por aluno/unidade permanecem inalterados.

O teste de integração da jornada acadêmica verifica o identificador da turma no boletim. Verificações de navegador com contas demonstrativas exercitam seletor, contadores, modais, rotas e persistência no HTTPS; não se deve interpretar simples quantidade de registros como prova de integração com serviços externos.
