# Clínica-Escola · Portal acadêmico e gestão clínica

[![CI Java e PostgreSQL](https://github.com/endeson12/clinica-escola-endeson/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/endeson12/clinica-escola-endeson/actions/workflows/tests.yml)
![Java 21](https://img.shields.io/badge/Java-21-2563eb)
![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-336791)
![Ambiente](https://img.shields.io/badge/ambiente-homologa%C3%A7%C3%A3o-d4a017)

**Gestão acadêmica, aprendizagem e operação clínica, com acesso por perfil e unidade.**

> Projeto independente de demonstração, sem integração institucional oficial. Não utilizar dados reais de pacientes. Documentos e financeiro são demonstrativos, sem validade oficial nem movimentação financeira real.

**[Abrir demonstração HTTPS](https://clinica-escola.76.13.234.134.sslip.io)** · **[Entregas e evidências](docs/ENTREGAS.md)** · **[Equipe](docs/EQUIPE-E-EXECUCAO.md)** · **[Como contribuir](CONTRIBUTING.md)** · **[CI](https://github.com/endeson12/clinica-escola-endeson/actions)**

## Visão do produto

| Frente | O que está disponível |
|---|---|
| Gestão acadêmica | Cursos, disciplinas, pessoas, turmas, matrículas e permissões |
| Aprendizagem | Aulas, materiais, atividades, entregas, avaliações e boletim |
| Serviços do portal | Meu curso, horários, calendário, solicitações, perfil e atividades complementares |
| Gestão clínica | Pacientes, especialidades, atendimentos e indicadores por unidade |
| Experiência | Interface responsiva, temas claro/escuro, navegação por teclado e identidade visual |
| Operação | Sessões PostgreSQL, CSRF, migrações Flyway, auditoria e testes automatizados |

**Perfis:** administração, coordenação, professor, aluno, financeiro e recepção. A visibilidade e as ações respeitam as permissões de cada perfil; nem todas as telas são oferecidas a todos.

## Equipe e frentes

| Integrante | Responsabilidade | Perfil |
|---|---|---|
| **Endeson Marcell** | Produto, prioridades, requisitos, critérios de aceite e apresentação | [@endeson12](https://github.com/endeson12) |
| **Eduardo** | Backend Java, APIs, dados, migrações e regras de negócio | [@fduduu](https://github.com/fduduu) |
| **Leônidas** | Frontend, integração das telas, responsividade, acessibilidade e experiência | [@ldevloper](https://github.com/ldevloper) |

**Execução assistida:** LEM, assistência de IA autorizada, identificada nas alterações correspondentes. A conta publicadora e a responsabilidade de frente não significam autoria manual ou revisão humana. O [registro da equipe](docs/EQUIPE-E-EXECUCAO.md) e o [mapa de entregas](docs/ENTREGAS.md) separam esses papéis.

## Evolução por etapas

| Etapa | Resultado | Evidência |
|---|---|---|
| Planejamento e qualidade | Proposta, equipe e CI Java/PostgreSQL | [PR9](https://github.com/endeson12/clinica-escola-endeson/pull/9), [PR11](https://github.com/endeson12/clinica-escola-endeson/pull/11), [PR12](https://github.com/endeson12/clinica-escola-endeson/pull/12) |
| Segurança | Renovação de CSRF e revogação de acesso | [PR13](https://github.com/endeson12/clinica-escola-endeson/pull/13), [PR14](https://github.com/endeson12/clinica-escola-endeson/pull/14) |
| Portal integrado | API acadêmica e interface para os perfis | [PR15](https://github.com/endeson12/clinica-escola-endeson/pull/15), [PR16](https://github.com/endeson12/clinica-escola-endeson/pull/16) |
| Identidade e experiência | Interface visual, marca, artes, temas e serviços internos | [PR17](https://github.com/endeson12/clinica-escola-endeson/pull/17), [PR18](https://github.com/endeson12/clinica-escola-endeson/pull/18), [PR19](https://github.com/endeson12/clinica-escola-endeson/pull/19) |
| Demonstração funcional | Cenário fictício aditivo e correções de semestre/avaliações | [PR20](https://github.com/endeson12/clinica-escola-endeson/pull/20), [cenário demonstrativo](docs/portal-academico/CENARIO-DEMONSTRATIVO.md) |

As PRs acima foram verificadas como integradas. A [matriz detalhada](docs/ENTREGAS.md) relaciona etapas, frentes, critérios e limites; não é um ranking artificial de commits.

## Arquitetura

```text
Navegador · HTML / CSS / JavaScript
              │ mesma origem · HTTPS · sessão + CSRF
Spring Boot · autenticação / autorização / APIs
              │ serviços e regras de negócio
PostgreSQL · dados / sessões / migrações Flyway
```

- **Backend:** Java21, Spring Boot4.1, Spring Security, validação e persistência.
- **Frontend:** HTML/CSS/JavaScript nativos, servidos pela aplicação Java.
- **Banco:** PostgreSQL17 na homologação Docker; isolamento por unidade na sessão.
- **Qualidade:** Maven, JUnit, PostgreSQL real na CI e aceitação em navegador.
- **Homologação atual:** Docker Compose e proxy HTTPS. Vercel/Neon constam na referência legada, não como descrição do ambiente Java atual.

## Executar e validar

1. Preparar JDK21 e PostgreSQL de desenvolvimento separado da homologação.
2. Configurar variáveis conforme os guias, sem publicar arquivos privados ou senhas.
3. Executar `./mvnw spring-boot:run` para desenvolvimento.
4. Executar `./mvnw verify` com o PostgreSQL de testes configurado para a suíte completa. Nunca apontar testes para dados de pacientes ou para o banco de homologação.

A CI usa um PostgreSQL descartável e publica relatórios JUnit. **Badge verde não substitui revisão funcional nem garante disponibilidade permanente da demonstração.**

## Documentação

| Precisa de… | Consulte |
|---|---|
| Entender todas as entregas | [Mapa de entregas](docs/ENTREGAS.md) |
| Entender responsabilidades e autoria | [Equipe e execução](docs/EQUIPE-E-EXECUCAO.md) |
| Propor uma mudança | [Guia de contribuição](CONTRIBUTING.md) |
| Operar o portal e conhecer limites | [Operação](docs/portal-academico/OPERACAO.md) |
| Configurar Docker, testes e backup | [Implantação](docs/portal-academico/IMPLANTACAO.md) |
| Conhecer os dados fictícios | [Cenário demonstrativo](docs/portal-academico/CENARIO-DEMONSTRATIVO.md) |
| Consultar o plano de evolução | [Plano integral](docs/portal-academico/PLANO-INTEGRAL-2026-11-07.md) |
| Consultar o projeto clínico original | [Referência legada preservada](docs/REFERENCIA-CLINICA-LEGADA.md) |

## Limites e uso responsável

- Não há SSO institucional, Teams, biblioteca ou pagamentos externos integrados.
- Carteirinha e resultados são demonstrativos; sem certificação ou emissão institucional.
- Contas e credenciais de demonstração são fornecidas em canal privado autorizado; não estão no repositório.
- Marcas, imagens e fontes mantêm os direitos de seus titulares; apresentação pública não implica licença institucional ou comercial.
- Alterações passam por branch, PR, verificação e leitura independente do resultado. Não há commits vazios, datas retroativas ou autoria fictícia para aumentar estatísticas.
