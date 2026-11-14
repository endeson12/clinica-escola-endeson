# Troca do docente responsável — 13/11/2026

## Escopo e autoria
Frente planejada: Eduardo/backend e segurança (#4), fluxo acadêmico (#7).
Execução REAL: LEM, assistência GPT-6.1 Sol/openai-codex. Sem autoria humana inferida.
Complemento da PR #22, sem código de produção, migração, alteração visual ou deploy.

## Regressão adicionada
`trocaDeDocenteRevogaAvaliacaoAnteriorEPreservaHistoricoParaNovoProfessor`:

1. Professor cria atividade, aluno entrega e professor publica avaliação.
2. Administração troca o responsável pela turma pela API real, sem SQL para simular a revogação.
3. A sessão antiga continua válida com perfil PROFESSOR, mas consultar entregas da atividade e alterar nota/feedback/publicação retorna 403.
4. PostgreSQL preserva nota, feedback e publicação após a tentativa indevida.
5. Novo professor, usando sessão já aberta antes da troca, consulta o histórico original e pode avaliar.
6. O boletim do aluno reflete a nova nota; o valor também é relido do PostgreSQL.

O teste passou no serviço existente. É cobertura adicional, não correção de vulnerabilidade nem ciclo TDD RED/GREEN. Nenhum comportamento de produção foi alterado para fabricar falha.

## Evidência local
- `mvn -B -ntp clean verify`, Java 21 e PostgreSQL privado exclusivo de teste.
- Recibo `baseline-20261113T121418Z.json`: **34 testes, zero falhas/erros/ignorados**, JAR produzido.
- Não inclui os três testes separados da PR #23.
- Avisos existentes Mockito/Byte Buddy permanecem; versões não alteradas.
- Backup do teste anterior e recibos privados fora do Git, em `daily-20261113/` no ambiente de auditoria.

## Gates e limites
CI do novo head, scanner redigido, revisão de diff e readback remoto devem ser conferidos na PR. Revisão assistida pela executora não é revisão externa nem aceite do PO. Issues #2/#4 permanecem em andamento até seus critérios integrais.
MockMvc e PostgreSQL isolado não comprovam navegador, implantação ou portal completo. Sem acesso a bancos de homologação/produção ou dados reais; sem alterações em cron.
