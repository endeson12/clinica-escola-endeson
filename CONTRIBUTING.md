# Como contribuir

O objetivo é entregar melhorias verificáveis, não atingir uma quota de commits. Consulte [equipe e responsabilidades](docs/EQUIPE-E-EXECUCAO.md) e [entregas existentes](docs/ENTREGAS.md).

## Fluxo de trabalho

1. Abra uma issue com problema, escopo e critérios de aceite; não inclua credenciais ou dados pessoais.
2. Combine o responsável da frente e verifique trabalho em andamento.
3. Crie branch da `main` atual: `feat/…`, `fix/…`, `docs/…` ou `test/…`.
4. Faça mudança coerente; correções devem ter reprodução ou regressão quando aplicável.
5. Execute verificações em ambiente isolado e revise `git diff --check`.
6. Faça commit com identidade verdadeira. Quando LEM implementar, declare autoria/execução assistida sem personificar colegas.
7. Abra PR com o template, resultados reais, riscos, limites e rollback quando necessário.
8. Aguarde checks do head atual. Revisão humana só pode ser registrada por quem realmente revisou.
9. Após merge/deploy, faça readback independente e atualize a issue conforme critérios cumpridos.

## Commits

Use mensagens claras: `fix(auth): renovar CSRF após login` ou `docs(portal): explicar limites da demonstração`. Separe mudanças com objetivos independentes; não fragmente uma alteração para multiplicar commits. Não crie commits vazios, datas retroativas, falsas aprovações ou coautoria sem participação real. Não faça force-push de histórico compartilhado para alterar estatísticas.

## Verificações

```bash
# PostgreSQL de TESTES configurado conforme o guia de implantação
./mvnw verify
git diff --check
```

- CI: Java21 e PostgreSQL descartável; conferir relatórios JUnit.
- Interface: teclado/foco, permissões, temas claro/escuro, desktop e mobile.
- APIs/dados: autorização, isolamento por unidade, relacionamentos e persistência após nova consulta.
- Dados demonstrativos: personagens fictícios, sem envio externo; preservar registros anteriores e backup antes de operação sensível.
- Não executar testes de escrita contra produção ou dados reais de pacientes.

## Segurança e privacidade

Não publique tokens, senhas, cookies, dumps, contas privadas de demonstração ou dados pessoais. Relatos de segurança devem omitir detalhes exploráveis em issues públicas e seguir canal privado autorizado pelo titular. Não há contato de segurança institucional inventado neste guia.

## Definição de concluído

- [ ] Escopo e critérios atendidos.
- [ ] Testes executados e resultados registrados sem inventar números.
- [ ] CI do head correto aprovada.
- [ ] Documentação atualizada quando necessária.
- [ ] Autoria, assistência e revisão descritas com precisão.
- [ ] Readback independente e rollback/backup, quando aplicáveis.

Veja [implantação](docs/portal-academico/IMPLANTACAO.md) e [operação](docs/portal-academico/OPERACAO.md).
