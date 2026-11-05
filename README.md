# Clínica-Escola | UNINASSAU

> **Projeto acadêmico demonstrativo.** Sem vínculo com sistemas oficiais da instituição. Não utilize dados reais de pacientes.

Sistema web interno para gerenciar **pacientes, especialidades e atendimentos** em duas unidades universitárias (UNINASSAU Jóquei e UNINASSAU Redenção).

- **Aplicação publicada:** https://clinica-escola-endeson.vercel.app
- **Repositório:** https://github.com/endeson12/clinica-escola-endeson (privado)

## Funcionalidades entregues

| # | Funcionalidade | Resumo |
|---|---|---|
| 1 | Login | Unidade + e-mail + senha, mostrar/ocultar senha, mensagens de erro, unidade não autorizada, logout |
| 2 | Pacientes | Cadastro com CPF validado (dígitos verificadores), CPF único por unidade, listagem da unidade |
| 3 | Especialidades | Cadastro com nome único, situação ativa/inativa, listagem |
| 4 | Atendimentos | Paciente + especialidade ativa + data/hora + status, listagem por unidade |
| + | Início (dashboard) | Unidade atual, atalhos e contadores reais |

## Stack

| Camada | Tecnologia |
|---|---|
| Frontend | HTML5, CSS3 e JavaScript nativo (Fetch API), servidos pelo próprio Spring Boot |
| Backend | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Security, Spring Data JPA, Bean Validation |
| Sessão | Spring Session JDBC (sessão guardada no PostgreSQL, cookie HttpOnly) |
| Banco | PostgreSQL (Neon, via Vercel Marketplace) com migrações Flyway |
| Build | Maven (Maven Wrapper incluso) |
| Hospedagem | Vercel, Container runtime (`Dockerfile.vercel`) |

## Organização do código

```
src/main/java/br/edu/clinicaescola/
├── ClinicaEscolaApplication.java   ponto de entrada
├── config/       conexão com o banco, cookie da sessão, usuários de demonstração
├── controller/   endpoints HTTP (/api/...)
├── service/      regras de negócio (inclui validação de CPF)
├── repository/   acesso ao banco (Spring Data JPA)
├── entity/       tabelas mapeadas (Unidade, Usuario, Paciente, Especialidade, Atendimento)
├── dto/          dados de entrada e saída da API
├── security/     Spring Security, usuário logado e unidade da sessão
└── exception/    tratamento padronizado de erros (JSON)

src/main/resources/
├── application.properties
├── db/migration/   V1__create_tables.sql, V2__seed_unidades.sql, V3__spring_session.sql
└── static/         index.html (login), dashboard.html, pacientes.html, especialidades.html,
                    atendimentos.html, css/style.css, js/*.js
```

## Endpoints da API

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| POST | `/api/auth/login` | público | Autenticar (unidade, e-mail, senha) |
| GET | `/api/auth/me` | logado | Consultar sessão (usuário e unidade) |
| POST | `/api/auth/logout` | logado | Encerrar sessão |
| GET | `/api/auth/csrf` | público | Emitir o token CSRF |
| GET | `/api/unidades` | público | Listar unidades (tela de login) |
| GET / POST | `/api/pacientes` | logado | Listar / cadastrar pacientes da unidade da sessão |
| GET / POST | `/api/especialidades` | logado | Listar (`?ativas=true`) / cadastrar especialidades |
| GET / POST | `/api/atendimentos` | logado | Listar / registrar atendimentos da unidade da sessão |
| GET | `/api/dashboard` | logado | Contadores da página inicial |
| GET | `/api/health` | público | Saúde da aplicação e do banco |

Respostas de erro seguem sempre o formato `{"status": 400, "message": "...", "fieldErrors": {"campo": "mensagem"}}`, com os códigos 400 (validação), 401 (sem login), 403 (sem permissão ou unidade não autorizada), 409 (duplicidade), 422 (relacionamento inválido) e 500 (falha interna).

## Decisões de arquitetura

- **Um único aplicativo Spring Boot** com separação por camadas (controller → service → repository → entity). Não há microsserviços.
- **API Gateway (simplificação):** todos os endpoints ficam centralizados sob `/api`, com o Spring Web como ponto de entrada único. **Não existe um Spring Cloud Gateway separado** nesta entrega; essa é uma simplificação consciente do MVP.
- **Mesma origem para frontend e backend:** as páginas são servidas pelo Spring Boot, sem CORS e com cookies de sessão simples.
- **Unidade na sessão do servidor:** a unidade escolhida no login fica na sessão. O navegador nunca informa `unidade_id` para cadastrar ou consultar, então um usuário não consegue ver dados de outra unidade.
- **Sessão no PostgreSQL (Spring Session JDBC):** na Vercel, a aplicação pode rodar em várias instâncias. Guardar a sessão no banco garante que o login continue válido em qualquer instância.
- **Segurança:** senhas com BCrypt, cookie `HttpOnly` + `Secure` (HTTPS) + `SameSite=Lax`, proteção CSRF (cookie `XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN`), troca de ID de sessão no login, mensagens genéricas para credenciais inválidas e validação de tudo no backend.
- **Banco versionado:** as tabelas são criadas pelas migrações SQL do Flyway (`ddl-auto=validate`); o Hibernate não cria tabelas.

## Como executar localmente

Pré-requisitos: **JDK 21**, **PostgreSQL 14+** (local ou Docker) e, opcionalmente, IntelliJ IDEA. O Maven não precisa estar instalado (use o `mvnw`).

1. Crie um banco vazio, por exemplo com Docker:
   ```bash
   docker run -d --name clinica-pg -e POSTGRES_DB=clinica -e POSTGRES_USER=clinica \
     -e POSTGRES_PASSWORD=clinica -p 5432:5432 postgres:17-alpine
   ```
2. Defina as variáveis de ambiente (modelo em `.env.example`):
   ```bash
   export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/clinica
   export DB_USERNAME=clinica DB_PASSWORD=clinica
   export DEMO_ADMIN_EMAIL=admin@clinica-escola.demo DEMO_ADMIN_PASSWORD='uma-senha-forte'
   ```
3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```
4. Acesse http://localhost:8080 e entre com o e-mail e a senha definidos.

As migrações criam as tabelas e as duas unidades automaticamente. O usuário administrador é criado (ou atualizado) a cada inicialização a partir das variáveis de ambiente.

### Testes

```bash
./mvnw test
```

- `CpfValidatorTest` sempre roda (validação de CPF).
- `ClinicaEscolaIntegrationTest` roda **contra um PostgreSQL real** quando `JDBC_DATABASE_URL` está definida. Ele cobre login correto e incorreto, e-mail inválido, unidade não autorizada, CPF inválido e duplicado, especialidade duplicada, atendimento com relacionamentos, paciente inexistente, isolamento entre unidades e logout.

Guia passo a passo do IntelliJ: [`docs/guia-intellij.md`](docs/guia-intellij.md).

## Deploy (Vercel)

1. Projeto Vercel `clinica-escola-endeson` com o arquivo `Dockerfile.vercel` na raiz. A Vercel constrói a imagem (build Maven com JDK 21 e execução com JRE 21) e a executa no Container runtime.
2. Banco Neon criado pelo Vercel Marketplace e conectado ao projeto. A Vercel injeta `DATABASE_URL_UNPOOLED` / `DATABASE_URL`, que a classe `DataSourceConfig` converte para JDBC com TLS (`sslmode=require`).
3. Variáveis configuradas no painel da Vercel: `DEMO_ADMIN_EMAIL`, `DEMO_ADMIN_PASSWORD`, `DEMO_RECEPCAO_EMAIL`, `DEMO_RECEPCAO_PASSWORD` e `COOKIE_SECURE=true`.
4. Publicação: `vercel deploy --prod` (ou push no GitHub, se o repositório estiver conectado).

Detalhes do banco: [`docs/banco-de-dados.md`](docs/banco-de-dados.md). Requisitos e critérios: [`docs/requisitos.md`](docs/requisitos.md). Evidências: [`docs/evidencias/`](docs/evidencias/).

## Responsabilidade de cada parte (explicação acadêmica)

- **Frontend (HTML, CSS, JS):** mostra as telas, valida os campos antes do envio para dar retorno rápido e conversa com o backend via `fetch`.
- **Backend (Spring Boot):** recebe as requisições, confere quem está logado e em qual unidade, aplica as regras (CPF válido, sem duplicidade, especialidade ativa, paciente da mesma unidade) e decide o que pode ser gravado.
- **Banco de dados (PostgreSQL):** guarda os dados de forma permanente e reforça as regras com chaves estrangeiras, restrições `UNIQUE` e `CHECK`.
- **Infraestrutura (Vercel + Neon):** publica a aplicação em HTTPS e mantém o banco gerenciado, para os dados sobreviverem a reinícios e novos deploys.
