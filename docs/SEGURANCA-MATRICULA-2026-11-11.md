# Revogação de matrícula e entrega — 11/11/2026

Incremento da PR #22, relacionado a #4/#2. Frente planejada: Eduardo/backend e segurança. Execução REAL: LEM, assistência GPT-6.1 Sol/openai-codex. Sem contribuição humana ou revisão independente atribuída.

## Cobertura acrescentada

Uma regressão HTTP/SQL com PostgreSQL real verifica o comportamento existente:

1. Professor cria duas atividades; aluno entrega a primeira.
2. Administração cancela a matrícula; a sessão do aluno permanece válida.
3. A mesma sessão não consegue entregar a segunda atividade (403); nenhuma entrega é persistida para ela.
4. A entrega anterior permanece no banco e acessível ao professor responsável.
5. O aluno perde a consulta explícita das entregas da turma (403), e a listagem geral não inclui o histórico da matrícula cancelada.

Cancelamento da matrícula não é revogação de sessão/unidade: são fronteiras distintas. O teste não amplia acesso ao histórico, nem modifica a política atual. Sem código de produção ou migração; cobertura de contrato existente, não correção de vulnerabilidade ou ciclo RED/GREEN de comportamento novo.

## Evidência local

`mvn -B -ntp clean verify`, Java 21 e PostgreSQL privado exclusivo de testes: **32 testes, zero falhas/erros/ignorados**, JAR produzido. Recibo privado `baseline-20261111T120537Z.json`. Avisos existentes Mockito/Byte Buddy permanecem. O total não inclui os três testes separados da PR #23.

Diff delimitado revisto pela própria LEM. CI do novo head e revisão externa/aceite são gates posteriores, registrados na PR e na issue #10 após readback. Não houve deploy, merge, navegador, acesso ao banco de homologação/produção, alteração visual ou de cron. Não comprova o portal completo nem aceite do PO.
