# Mapa de entregas · equipe, etapas e evidências

Este índice apresenta trabalho registrado no GitHub. Não recria PRs antigas, não altera autoria e não atribui revisão humana sem evidência. Os estados abaixo foram consultados diretamente no repositório nesta organização.

## Participação e rastreabilidade

| Frente | Responsável designado | Contribuição reconhecida |
|---|---|---|
| Produto e coordenação | Endeson Marcell · [endeson12](https://github.com/endeson12) | Direcionamento de escopo, prioridades e critérios de aceite recebidos no canal do projeto |
| Backend e dados | Eduardo · [fduduu](https://github.com/fduduu) | Frente designada para APIs, regras, migrações e segurança; publicações conforme autorização registrada |
| Frontend e experiência | Leônidas · [ldevloper](https://github.com/ldevloper) | Frente designada para interface, responsividade, integração visual e usabilidade; publicações conforme autorização |
| Implementação e verificações assistidas | LEM | Execução assistida explicitada nos commits, documentação e PRs correspondentes |

Responsabilidade, publicação, autoria e revisão são campos diferentes. Registrar o que a pessoa realmente fez; não converter assignee ou conta publicadora em evidência de programação manual.

## Linha de entregas integradas

Todos os links abaixo apontam para PRs mescladas. “Integrada” descreve o código/documentação no Git, não auditoria institucional nem disponibilidade contínua do site.

| PR | Etapa | Entrega verificável | Frente |
|---|---|---|---|
| [9](https://github.com/endeson12/clinica-escola-endeson/pull/9) | Descoberta | Proposta de evolução do portal acadêmico | Produto |
| [11](https://github.com/endeson12/clinica-escola-endeson/pull/11) | Fundação | Baseline PostgreSQL, CI, equipe e rotina verificável | Qualidade / integração |
| [12](https://github.com/endeson12/clinica-escola-endeson/pull/12) | Organização | Confirmação das contas de Eduardo e Leônidas | Equipe |
| [13](https://github.com/endeson12/clinica-escola-endeson/pull/13) | Segurança | Renovação do token CSRF após login | Backend |
| [14](https://github.com/endeson12/clinica-escola-endeson/pull/14) | Segurança | Encerramento de sessões quando o acesso é revogado | Backend |
| [15](https://github.com/endeson12/clinica-escola-endeson/pull/15) | Acadêmico | API, modelos e permissões por unidade | Backend / dados |
| [16](https://github.com/endeson12/clinica-escola-endeson/pull/16) | Integração | Interface conectada ao portal acadêmico | Frontend |
| [17](https://github.com/endeson12/clinica-escola-endeson/pull/17) | Experiência | Portal visual interativo e evidências de navegador | Frontend |
| [18](https://github.com/endeson12/clinica-escola-endeson/pull/18) | Identidade | Marca e oito artes fornecidas | Frontend |
| [19](https://github.com/endeson12/clinica-escola-endeson/pull/19) | Serviços e UX | Temas, refinamento da experiência e serviços acadêmicos internos | Frontend / integração |
| [20](https://github.com/endeson12/clinica-escola-endeson/pull/20) | Cenário demonstrativo | Vínculo de notas, semestre atual e documentação do cenário fictício | Backend / frontend / qualidade |

A carga demonstrativa foi uma operação autorizada no banco, não um dump público. A PR20 registra documentação e correções associadas; não presumir que seu diff contém a carga privada.

## Critérios de uma entrega profissional

| Gate | Evidência necessária |
|---|---|
| Escopo | Issue ou descrição com problema, comportamento esperado e critérios de aceite |
| Implementação | Diff real, delimitado, com autoria correta e assistência declarada |
| Testes | Comando executado e resultado; para API/dados, banco isolado |
| Integração | PR com riscos, limites e checks do head correto |
| Publicação | Quando houver deploy, readback independente do serviço e rollback preservado |
| Participação | Registro da ação efetiva; revisão somente quando realizada |

Não encerrar automaticamente uma issue ampla só porque uma PR relacionada foi integrada. Critérios não atendidos continuam pendentes.

## Reconhecimento de novas contribuições

- **Endeson:** requisitos, decisões e aceite podem ser registrados como comentários próprios e evidências de validação, sem inventar código.
- **Eduardo:** incrementos reais de backend/dados devem incluir teste e PR da frente, com execução descrita corretamente.
- **Leônidas:** incrementos reais de interface devem incluir integração com API e verificação desktop/mobile.
- **Revisão humana:** o integrante lê o diff, testa quando aplicável e registra a análise na própria conta. Automação não submete aprovações em nome da equipe.
- **IA:** commits produzidos por LEM mantêm essa autoria; responsável operacional continua separado.

## Gráficos do GitHub

A tabela da equipe é uma apresentação editorial: não altera o gráfico Contributors nem o calendário de contribuições. Essas métricas são calculadas pelo GitHub a partir de atividades elegíveis e identidades vinculadas às contas. Não prometemos quantidade, inclusão de nomes ou atualização instantânea. Não reescrevemos commits antigos nem acrescentamos coautores que não participaram.

Consulte [equipe e autorização](EQUIPE-E-EXECUCAO.md), [contribuição](../CONTRIBUTING.md) e [operação](portal-academico/OPERACAO.md).
