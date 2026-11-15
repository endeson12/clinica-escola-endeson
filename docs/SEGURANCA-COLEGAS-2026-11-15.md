# Isolamento entre colegas da mesma turma — 15/11/2026

## Incremento

Cobertura adicional de segurança da PR #22, relacionada às issues #2/#4 e à jornada acadêmica #7. Responsável planejado: Eduardo/backend e dados; executor real: LEM, assistência GPT-6.1 Sol/openai-codex. Nenhuma autoria humana inferida.

O novo teste `colegasDaMesmaTurmaRecebemSomenteSuasEntregasENotas` cria dois alunos matriculados na mesma turma, duas entregas para a mesma atividade e avaliações publicadas distintas. Confere que cada sessão recebe exatamente sua própria entrega, texto, nota e feedback em consultas filtradas e não filtradas; no boletim, cada sessão recebe somente a avaliação própria. Consultar boletim do colega por ID ou tentar alterar sua avaliação retorna 403 nas duas direções. Consulta docente mantém duas entregas e PostgreSQL preserva texto, nota, feedback e publicação após as tentativas.

## Validação e limites

- Execução: Java 21/Maven fixado por digest e PostgreSQL privado de teste, via cópia em memória do runner de baseline com `mvn -B -ntp clean verify` e caminho da worktree próprios. Runner original e árvores concorrentes preservados.
- A primeira tentativa excedeu o limite da ferramenta sem produzir recibo completo; XMLs parciais não foram tratados como build aprovado. Nova execução rastreada separadamente; resultado e CI serão registrados na PR e issue #10 após leitura dos recibos completos.
- Cobertura de comportamento existente, não correção de vulnerabilidade; nenhum código de produção foi alterado e não se alega ciclo RED/GREEN de implementação.
- MockMvc usa filtros reais, serviços e PostgreSQL; não comprova navegador, cache frontend, aceite do PO ou implantação.
- Dados exclusivamente fictícios; sem produção/homologação, migração, alteração visual, serviços pagos ou cron.

## Próximo gate

Revisão externa real e aceite do head testado antes de integrar. Issues #2/#4 permanecem em andamento; esta cobertura não certifica o portal completo nem sua entrega em 20/11.
