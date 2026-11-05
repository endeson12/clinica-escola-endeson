PRD MESTRE — PROJETO RÁPIDO PARA O ENDESON
Sistema de Clínica-Escola | UNINASSAU
Responsável pela execução: A.N.A.
Solicitante: Jota
Participante para validações: Endeson
Data: 05/11/2026
Prioridade: Máxima — entrega acadêmica hoje
Modo: Execução autônoma, individual e de ponta a ponta
1. DIRETRIZ PRINCIPAL PARA A A.N.A.
A.N.A., você está recebendo uma missão de desenvolvimento com escopo fechado e prazo de entrega no mesmo dia.
Sua responsabilidade é planejar, implementar, testar, versionar, hospedar e documentar um sistema acadêmico de Clínica-Escola para a UNINASSAU.
Você deverá executar o projeto pessoalmente, utilizando suas ferramentas disponíveis.
Regras obrigatórias:
Não delegue desenvolvimento, planejamento ou execução a outros agentes, subagentes ou CLIs de inteligência artificial, incluindo Codex, Claude Code, OpenCode ou equivalentes.
Você pode utilizar ferramentas de terminal convencionais, Maven, Git, Docker e ferramentas de deploy para executar comandos técnicos. Isso não constitui delegação a outra IA.
Não envolva Argos, outros agentes Hermes ou qualquer outro responsável.
Crie e mantenha seu próprio checklist de tarefas, organizado por fases e critérios de aceite.
Execute sequencialmente, sem solicitar aprovação entre cada fase.
Resolva autonomamente problemas de implementação, configuração, dependências e integração sempre que tiver autorização e ferramentas suficientes.
Não substitua o backend Java ou PostgreSQL por mocks ou soluções diferentes apenas para concluir mais rápido.
Priorize funcionamento real e simplicidade. Evite arquitetura desnecessariamente complexa.
Não considere uma funcionalidade concluída sem testá-la.
Não declare deploy concluído sem verificar o endereço público, a conexão com o banco e as funcionalidades principais.
O Endeson estará no grupo e poderá esclarecer requisitos ou solicitar pequenas alterações. Trate essas intervenções como atualizações do escopo, avaliando se cabem no prazo.
O objetivo é entregar uma aplicação funcional, e não apenas gerar código ou uma interface de demonstração.
2. CONTEXTO E OBJETIVO DO PRODUTO
Desenvolver um sistema web de Clínica-Escola para utilização acadêmica na UNINASSAU.
O sistema representará uma aplicação interna de gerenciamento de pacientes, especialidades e atendimentos em duas unidades universitárias.
O professor definiu a necessidade de frontend, backend, banco PostgreSQL, organização do código, integração, GitHub e hospedagem.
A entrega emergencial deverá contemplar somente quatro funcionalidades:
Tela de login.
Cadastro de pacientes.
Cadastro de especialidades.
Cadastro de atendimentos.
A aplicação deve possuir banco de dados persistente, autenticação funcional, validações de formulário e uma interface visual consistente.
Definição de sucesso: um avaliador consegue acessar a aplicação publicada, entrar com uma conta demonstrativa, cadastrar dados e comprovar que esses dados foram persistidos no PostgreSQL.
3. ESCOPO FUNCIONAL
RF-01 — Tela de login
Criar uma tela de autenticação com identidade visual inspirada nas referências enviadas pelo Endeson e no portal acadêmico informado:
https://fs-educ.sempreser.com.br/adfs/ls/
O portal é apenas uma referência visual. Não integrar com o sistema de autenticação real da instituição.
Campos obrigatórios:
Unidade: UNINASSAU Jóquei ou UNINASSAU Redenção.
E-mail do usuário.
Senha.
Botão "Entrar".
Comportamentos obrigatórios:
Validar formato do e-mail.
Impedir envio de campos vazios.
Exibir mensagem clara para credenciais incorretas.
Exibir mensagem quando uma unidade não estiver autorizada para o usuário.
Permitir mostrar ou ocultar senha.
Informar quando o login estiver sendo processado.
Redirecionar para a área interna após autenticação.
Permitir encerramento da sessão.
Impedir acesso às áreas internas sem autenticação.
Decisão sobre requisitos conflitantes:
A solicitação inicial mencionava CPF ou matrícula no login. A instrução posterior foi explícita: login por e-mail.
Portanto, adotar e-mail e senha para autenticação. O CPF será obrigatório no cadastro de pacientes.
Não implementar login por CPF ou matrícula nesta entrega, salvo nova determinação expressa do Endeson.
RF-02 — Cadastro de pacientes
Criar uma página de cadastro de pacientes.
Campos:
Nome completo — obrigatório.
CPF — obrigatório.
E-mail — obrigatório.
Telefone — opcional.
Data de nascimento — opcional.
Unidade — associada automaticamente à unidade selecionada na sessão.
Validações:
CPF brasileiro com validação dos dígitos verificadores.
Rejeitar CPF inválido e sequências repetidas.
Normalizar CPF antes de salvar.
Impedir CPF duplicado dentro da mesma unidade.
Validar e-mail.
Impedir nome vazio.
Mostrar mensagens de erro por campo.
Exibir confirmação após cadastro.
Além do formulário, implementar uma tabela simples para listar os pacientes cadastrados na unidade atual.
Permitir consultar os registros após atualizar a página, comprovando persistência real.
Não usar CPFs, e-mails ou dados clínicos reais na demonstração.
RF-03 — Cadastro de especialidades
Criar página para cadastrar especialidades oferecidas pela Clínica-Escola.
Campos:
Nome da especialidade — obrigatório.
Descrição — opcional.
Situação — ativa/inativa.
Requisitos:
Impedir nome vazio.
Evitar especialidades duplicadas.
Salvar no PostgreSQL.
Listar especialidades cadastradas.
Disponibilizar apenas especialidades ativas para novos atendimentos.
Pode utilizar exemplos fictícios como Fisioterapia, Psicologia e Nutrição, sem presumir que constituam ofertas reais da instituição.
RF-04 — Cadastro de atendimentos
Criar página para registrar atendimentos relacionados aos pacientes cadastrados.
Campos:
Paciente — seleção a partir do banco.
Especialidade — seleção a partir do banco.
Data e horário do atendimento.
Status: Agendado, Realizado ou Cancelado.
Observações — opcional.
Unidade — atribuída pela sessão.
Requisitos:
Um atendimento deve estar associado a um paciente existente.
A especialidade deve existir e estar ativa no momento do cadastro.
Não permitir atendimento com campos obrigatórios vazios.
Verificar se o paciente pertence à unidade selecionada.
Registrar os dados no PostgreSQL.
Listar os atendimentos cadastrados.
Exibir paciente, especialidade, data, status e unidade.
Não implementar prontuário médico, prescrição, faturamento ou dados clínicos sensíveis.
RF-05 — Dashboard interno
Criar uma página inicial extremamente simples após o login.
Apresentar quatro elementos:
Identificação do sistema e unidade selecionada.
Navegação para pacientes.
Navegação para especialidades.
Navegação para atendimentos.
Opcionalmente, exibir contadores reais de pacientes e atendimentos, se isso não comprometer a entrega.
A prioridade é uma navegação clara entre as funcionalidades.
4. IDENTIDADE VISUAL E FRONTEND
Tecnologias obrigatórias:
HTML5.
CSS3.
JavaScript.
Layout responsivo.
Utilizar os materiais e capturas de tela enviados pelo Endeson como referência visual.
A.N.A., identifique a paleta presente nas referências. Não invente cores institucionais afirmando que são oficiais.
Na ausência de especificações exatas, utilize uma composição visual baseada em azul institucional, branco e tons neutros.
Orientação de design:
Interface acadêmica, profissional e limpa.
Tela de login com identidade visual própria, inspirada no portal.
Formulários centralizados em cards.
Navegação lateral ou superior simples.
Campos com labels visíveis.
Botões e estados de foco consistentes.
Mensagens de erro próximas aos campos.
Tabelas legíveis e adaptáveis a celulares.
Feedback visual para salvamento, falha e carregamento.
Não copiar marcas, elementos proprietários ou telas do portal institucional de forma que o protótipo possa ser confundido com um serviço oficial.
Identificar discretamente o ambiente como "Projeto acadêmico demonstrativo".
Priorizar boa experiência em computadores, sem deixar o acesso pelo celular quebrado.
Não utilizar React, Next.js ou outras estruturas que não agreguem valor ao escopo. A aplicação pode usar páginas HTML, CSS e JavaScript servidas pelo próprio Spring Boot.
5. ARQUITETURA TÉCNICA
5.1 Stack
Backend:
Java 21 LTS.
Spring Boot estável e compatível.
Spring Web.
Spring Security.
Spring Data JPA.
Bean Validation.
Maven.
Frontend:
HTML, CSS e JavaScript nativos.
Fetch API para comunicação com o backend.
Banco de dados:
PostgreSQL.
Migrações SQL versionadas usando Flyway.
Credenciais por variáveis de ambiente.
Infraestrutura:
GitHub para versionamento.
Vercel para hospedagem.
PostgreSQL gerenciado, preferencialmente Neon via Vercel Marketplace.
Dockerfile.vercel para executar o Spring Boot.
5.2 Padrão de arquitetura
Utilizar um único aplicativo Spring Boot, com separação por responsabilidades e módulos internos.
Organizar em:
Controllers: endpoints HTTP.
Services: regras de negócio.
Repositories: operações de banco.
Entities: modelos persistidos.
DTOs: entrada e saída de dados.
Security: autenticação e proteção.
Exceptions: tratamento consistente de erros.
O professor mencionou API Gateway.
Para este MVP, centralizar os endpoints sob /api, utilizando o Spring Web como ponto de entrada da aplicação.
Documentar no README que essa é uma simplificação arquitetural. Não alegar que existe um Spring Cloud Gateway independente caso ele não tenha sido implementado.
Não criar microsserviços apenas para justificar o uso de um gateway.
5.3 Autenticação
Utilizar Spring Security com autenticação por e-mail e senha.
Requisitos mínimos:
Senhas armazenadas com BCrypt ou equivalente seguro.
Sessão autenticada em cookie HttpOnly.
Cookie Secure no ambiente HTTPS.
Proteção adequada contra CSRF.
Logout que invalide a sessão.
Endpoints privados protegidos no backend.
Mensagens genéricas para credenciais inválidas.
Permissões verificadas no servidor.
Criar um usuário administrador demonstrativo, autorizado a acessar as duas unidades.
A conta deve ser inicializada de forma idempotente a partir de variáveis de ambiente, sem senha fixa publicada no repositório.
Não salvar senhas reais em texto puro, nem no código, nem nos scripts SQL.
5.4 Organização da API
Implementar endpoints equivalentes a:
Método
Endpoint
Responsabilidade
POST
/api/auth/login
Autenticar
GET
/api/auth/me
Consultar sessão
POST
/api/auth/logout
Encerrar sessão
GET
/api/unidades
Listar unidades
GET
/api/pacientes
Listar pacientes
POST
/api/pacientes
Cadastrar paciente
GET
/api/especialidades
Listar especialidades
POST
/api/especialidades
Cadastrar especialidade
GET
/api/atendimentos
Listar atendimentos
POST
/api/atendimentos
Cadastrar atendimento
GET
/api/health
Verificar aplicação
A implementação de autenticação pode usar handlers e filtros do Spring Security, desde que preserve o contrato funcional esperado pelo frontend.
Utilizar respostas JSON consistentes e códigos HTTP apropriados para validação, autenticação, conflito e falhas internas.
5.5 Regras de unidade
As duas unidades deverão ser previamente cadastradas no banco.
Após autenticação, a unidade selecionada deve permanecer associada à sessão do usuário no servidor.
Cadastros e consultas de pacientes e atendimentos devem respeitar essa unidade.
Não confiar em um unidade_id enviado livremente pelo navegador para autorizar acesso aos registros.
6. MODELAGEM DO BANCO POSTGRESQL
Criar as seguintes tabelas:
unidades
id
nome
codigo
usuarios
id
nome
email
senha_hash
ativo
criado_em
usuarios_unidades
usuario_id
unidade_id
pacientes
id
unidade_id
nome_completo
cpf
email
telefone
data_nascimento
criado_em
especialidades
id
nome
descricao
ativa
criado_em
atendimentos
id
unidade_id
paciente_id
especialidade_id
data_hora
status
observacoes
criado_em
Regras do banco:
Criar primary keys e foreign keys reais.
Criar restrição de CPF único por unidade.
Criar restrição para e-mail único de usuário.
Impedir referências a pacientes e especialidades inexistentes.
Utilizar status de atendimento com valores controlados.
Utilizar migrações SQL reproduzíveis.
Criar seed para as duas unidades.
Criar usuário de demonstração com senha derivada de variável de ambiente, e não senha em texto puro no SQL.
Garantir que os dados persistam após reinicialização ou redeploy.
Obrigatório: entregar os scripts SQL como arquivos .sql separados e acessíveis no repositório.
Não depender exclusivamente de criação automática de tabelas pelo Hibernate.
7. ORGANIZAÇÃO DOS ARQUIVOS
Criar uma pasta independente chamada:
projeto rapido pro endeson
Ela não deve ser incorporada ao código-fonte de outros projetos Hermes.
Estrutura recomendada:
projeto rapido pro endeson/
│
├── README.md
├── PRD.md
├── CHECKLIST.md
├── .gitignore
├── .env.example
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
├── Dockerfile.vercel
├── vercel.json (se necessário)
│
├── src/
│   ├── main/
│   │   ├── java/br/edu/clinicaescola/
│   │   │   ├── ClinicaEscolaApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── security/
│   │   │   └── exception/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/
│   │       │   ├── V1__create_tables.sql
│   │       │   └── V2__seed_unidades.sql
│   │       └── static/
│   │           ├── index.html
│   │           ├── dashboard.html
│   │           ├── pacientes.html
│   │           ├── especialidades.html
│   │           ├── atendimentos.html
│   │           ├── css/
│   │           │   └── style.css
│   │           └── js/
│   │               ├── api.js
│   │               ├── auth.js
│   │               ├── pacientes.js
│   │               ├── especialidades.js
│   │               └── atendimentos.js
│   │
│   └── test/
│       └── java/
│
└── docs/
    ├── requisitos.md
    ├── guia-intellij.md
    ├── banco-de-dados.md
    └── evidencias/
A estrutura é uma referência. Simplifique arquivos vazios ou desnecessários, mas preserve a separação por responsabilidade.
Cada classe, página e script deve existir como arquivo individual.
O projeto deve abrir diretamente no IntelliJ por meio do pom.xml.
Observação: "Extension Pack for Java" e "Spring Boot Extension Pack" são extensões do VS Code, não requisitos de instalação do IntelliJ. Não perder tempo tentando instalá-las no IntelliJ.
8. EXECUÇÃO POR FASES
FASE 0 — Inicialização e planejamento
Objetivo: preparar o projeto sem interferir em projetos existentes.
Tarefas:
Analisar todas as mensagens e referências do Endeson.
Consolidar os requisitos deste PRD.
Criar a pasta solicitada.
Criar PRD.md dentro dela.
Criar CHECKLIST.md com fases e tarefas.
Verificar os acessos disponíveis ao GitHub e à Vercel do Jota.
Verificar se já existe algum projeto ou repositório com o nome pretendido.
Definir os nomes dos novos recursos.
Identificar eventuais limitações de ferramentas.
Nomes sugeridos:
Repositório: clinica-escola-endeson
Projeto Vercel: clinica-escola-endeson
Nome exibido: Clínica-Escola | UNINASSAU
Criar novos recursos. Não modificar nenhum repositório, deployment ou banco de outro projeto.
Critério de aceite: escopo consolidado, pasta criada e checklist registrado.
FASE 1 — Estrutura Spring Boot e banco de dados
Objetivo: estabelecer uma aplicação Java funcional conectada ao PostgreSQL.
Tarefas:
Criar projeto Maven com Java 21.
Adicionar dependências necessárias.
Criar ponto de entrada da aplicação.
Configurar variáveis de ambiente.
Criar entidades JPA.
Criar repositories.
Criar migrações SQL.
Inicializar unidades.
Implementar health check.
Testar conexão com PostgreSQL.
Compilar com Maven.
Executar a aplicação localmente.
Utilizar conexão PostgreSQL compatível com JDBC e TLS quando exigido pelo provedor.
Configurar um pool de conexões pequeno, apropriado para a execução em contêineres sob demanda.
Critério de aceite: aplicação inicia, conecta ao banco, executa as migrações e responde ao health check.
FASE 2 — Validar hospedagem antecipadamente
Objetivo: evitar descobrir problemas de Vercel somente no final.
Tarefas:
Criar Dockerfile.vercel adequado ao Spring Boot.
Configurar o servidor para escutar a porta indicada por PORT.
Verificar compatibilidade do contêiner com o runtime da Vercel.
Criar um novo projeto na conta correta.
Configurar variáveis de ambiente.
Fazer um primeiro deployment técnico.
Verificar health check por HTTPS.
Utilizar o suporte a contêineres da Vercel. Não tentar implantar Java como se fosse uma aplicação estática.
Validar limites e possíveis cobranças do plano. Não contratar recursos pagos ou alterar assinaturas sem autorização explícita do Jota.
Critério de aceite: aplicação Spring Boot responde publicamente no novo projeto Vercel.
Caso a implantação esteja bloqueada por plano, permissões ou falha de plataforma, registrar o impedimento e continuar as demais fases. Não substituir automaticamente a stack solicitada.
FASE 3 — Autenticação
Objetivo: permitir entrada real no sistema.
Tarefas:
Implementar configuração Spring Security.
Criar usuário de demonstração.
Implementar autenticação por e-mail e senha.
Verificar associação do usuário à unidade.
Implementar consulta da sessão.
Implementar logout.
Proteger endpoints internos.
Validar erros de credenciais.
Testar autenticação no backend.
Critério de aceite: credenciais válidas autenticam, inválidas falham e requisições sem sessão não acessam os dados internos.
FASE 4 — APIs de cadastro
Objetivo: implementar as três funcionalidades de persistência.
Ordem obrigatória:
4.1 Especialidades
Endpoint de cadastro.
Endpoint de listagem.
Validações.
Teste de gravação.
4.2 Pacientes
Endpoint de cadastro.
Endpoint de listagem.
Validação de CPF e e-mail.
Validação de duplicidade.
Isolamento por unidade.
Teste de gravação.
4.3 Atendimentos
Endpoint de cadastro.
Endpoint de listagem.
Relacionamentos com paciente e especialidade.
Validação de unidade.
Validação de status e data.
Teste de gravação.
Critério de aceite: todas as operações criam registros persistidos e retornam dados reais do PostgreSQL.
FASE 5 — Frontend e integração
Objetivo: entregar a experiência utilizável.
Tarefas:
Criar tela de login.
Aplicar paleta e layout das referências.
Criar dashboard.
Criar página de pacientes.
Criar página de especialidades.
Criar página de atendimentos.
Integrar formulário de login.
Integrar os três cadastros com APIs reais.
Implementar mensagens de erro e sucesso.
Implementar carregamento e listas.
Corrigir problemas de responsividade.
Proteger a navegação interna.
O frontend e o backend devem preferencialmente compartilhar a mesma origem, reduzindo complexidade com CORS e cookies.
Não manter dados definitivos apenas no navegador.
Critério de aceite: é possível concluir todos os fluxos usando somente a interface publicada.
FASE 6 — GitHub e versionamento
Objetivo: fornecer comprovação do código-fonte.
Tarefas:
Inicializar Git.
Criar .gitignore para Java, IntelliJ, arquivos locais e segredos.
Revisar o código e retirar credenciais.
Criar novo repositório na conta GitHub do Jota.
Publicar os arquivos organizados.
Confirmar que o repositório contém código e scripts SQL.
Registrar commits com descrições compreensíveis.
Atualizar README.
Associar repositório ao projeto Vercel, quando possível.
Não usar um repositório preexistente do Endeson sem receber o endereço e a autorização correta.
Inicialmente manter o novo repositório privado, podendo ajustar a visibilidade para atender à entrega acadêmica mediante autorização.
Critério de aceite: repositório acessível ao proprietário, contendo arquivos reais e histórico de commits.
FASE 7 — Deploy completo e validação em produção
Objetivo: colocar a versão final no ar.
Tarefas:
Atualizar o deployment com todas as funcionalidades.
Conferir variáveis de ambiente do PostgreSQL.
Verificar HTTPS.
Verificar autenticação.
Cadastrar uma especialidade fictícia.
Cadastrar um paciente fictício.
Cadastrar um atendimento fictício.
Atualizar a página e confirmar persistência.
Conferir registros diretamente no banco.
Verificar erros do backend nos logs.
Testar logout.
Testar o acesso não autenticado.
Verificar a navegação em celular e desktop.
Não usar banco local como se fosse o banco de produção.
Critério de aceite: todas as quatro funcionalidades funcionam no endereço público, com persistência real.
FASE 8 — Documentação e evidências
Objetivo: permitir que o Endeson e o grupo apresentem o projeto.
Preparar:
README explicando objetivo e stack.
Instruções de instalação no IntelliJ.
Pré-requisitos de Java, Maven e PostgreSQL.
Instruções para configurar .env local.
Comandos para executar e testar.
Explicação das tabelas e relacionamentos.
Lista dos endpoints.
Instruções para reproduzir a configuração do banco.
Explicação do deploy.
Screenshots das quatro telas.
Evidências de operações funcionais.
Evidência da aplicação publicada.
Link do repositório.
Não incluir senhas, tokens, strings de conexão ou dados pessoais nas evidências.
Criar uma explicação acadêmica simples sobre as responsabilidades de frontend, backend, banco de dados e infraestrutura.
Documentar também as decisões arquiteturais, incluindo a simplificação do API Gateway.
Critério de aceite: outra pessoa consegue entender a organização do projeto, abrir o código e demonstrar as funcionalidades sem depender da A.N.A.
9. TESTES E CRITÉRIOS DE ACEITE GERAIS
Executar e registrar obrigatoriamente:
Teste
Resultado esperado
Login correto
Acesso permitido
Senha incorreta
Erro claro, sem autenticação
E-mail inválido
Validação do formulário
Unidade não autorizada
Acesso negado
Página interna sem sessão
Acesso bloqueado
CPF inválido
Cadastro rejeitado
CPF duplicado na unidade
Cadastro rejeitado
Paciente válido
Registro persistido
Especialidade válida
Registro persistido
Atendimento válido
Registro com relacionamentos corretos
Atendimento com paciente inexistente
Rejeitado
Consulta de outra unidade
Dados não expostos
Atualização da página
Registros continuam disponíveis
Logout
Sessão encerrada
Abertura pelo IntelliJ
Projeto Maven reconhecido
Deploy Vercel
Aplicação funcional por HTTPS
Implementar testes automatizados para as validações críticas sempre que possível.
Priorizar cobertura de validação de CPF, autenticação, isolamento por unidade e relacionamentos no banco.
Executar testes manuais end-to-end no deployment publicado.
Não marcar testes como aprovados com base apenas na leitura do código.
10. REQUISITOS NÃO FUNCIONAIS
Desempenho: a aplicação deve ser leve, sem bibliotecas pesadas desnecessárias. Evitar requisições redundantes e consultas N+1.
Manutenibilidade: nomes claros, classes pequenas, responsabilidades separadas e código compreensível por estudantes.
Segurança: sessões protegidas, senhas com hash, validação no backend, consultas parametrizadas via JPA e proteção de credenciais.
Banco: migrations versionadas, constraints e relacionamentos corretos.
Usabilidade: layout intuitivo, validações compreensíveis e estados de carregamento.
Portabilidade: execução local no IntelliJ sem dependência obrigatória da Vercel.
Hospedagem: aplicação funcional em URL HTTPS, com persistência externa.
Privacidade: ambiente exclusivamente demonstrativo, sem dados reais de pacientes.
11. RESTRIÇÕES E FORA DO ESCOPO
Não desenvolver nesta versão:
Prontuário eletrônico.
Integração com sistemas reais da UNINASSAU.
Login pelo portal institucional.
Pagamentos e faturamento.
Prescrições.
Agendamento avançado.
Notificações por WhatsApp ou e-mail.
Aplicativo móvel.
Relatórios avançados.
Microsserviços.
API Gateway separado.
Sistema completo de permissões clínicas.
Edição e exclusão de todos os registros, exceto se houver necessidade técnica ou tempo disponível após concluir os requisitos obrigatórios.
Não ampliar o escopo sem motivo.
Cada nova dependência, tela ou módulo deve ser avaliado pelo impacto na entrega de hoje.
12. ESTRATÉGIA DE PRIORIZAÇÃO
Classificar as tarefas em:
P0 — Obrigatórias
Aplicação Java executável.
Banco PostgreSQL conectado.
Autenticação funcional.
Seleção de unidade.
Cadastro e listagem de pacientes.
Cadastro e listagem de especialidades.
Cadastro e listagem de atendimentos.
Validações essenciais.
Repositório GitHub.
Deploy acessível.
Testes mínimos.
README de execução.
P1 — Importantes
Layout fiel às referências.
Refinamento responsivo.
Mensagens de sucesso aprimoradas.
Screenshots e documentação de apresentação.
Testes automatizados complementares.
P2 — Opcionais
Indicadores no dashboard.
Busca e filtros avançados.
Edição e exclusão.
Animações.
Melhorias visuais adicionais.
Concluir todos os P0 antes de investir significativamente nos P2.
Se houver bloqueio de infraestrutura, continuar a execução das demais tarefas e registrar o bloqueio.
Se o prazo estiver terminando, preservar funcionamento, integridade dos dados, código versionado e documentação básica. Nunca substituir uma funcionalidade real por uma simulação silenciosa.
13. POLÍTICA DE AUTONOMIA E COMUNICAÇÃO
A.N.A., a partir do início desta missão:
Você está autorizada a:
Criar arquivos e pastas do projeto.
Escrever e editar todo o código.
Executar testes e builds.
Corrigir bugs.
Criar um novo repositório GitHub na conta disponibilizada.
Criar um novo projeto Vercel na conta disponibilizada.
Configurar integração com Git.
Criar e configurar recursos de banco gratuitos, caso disponíveis e autorizados.
Fazer commits e deploys deste projeto.
Atualizar o checklist.
Produzir documentação e evidências.
Você não está autorizada a:
Apagar ou modificar projetos já existentes.
Contratar serviços pagos ou alterar assinaturas sem aprovação.
Expor tokens, credenciais ou dados pessoais.
Utilizar credenciais de outros projetos sem autorização.
Delegar tarefas a outra IA.
Declarar funcionalidades como prontas sem testes.
Realizar alterações fora do escopo.
Quando existir um impedimento que dependa exclusivamente de um humano, como autenticação, aprovação financeira, autorização de acesso ou solicitação de permissão, comunique o bloqueio de forma objetiva e continue as tarefas independentes.
Não interrompa a execução para solicitar preferências de cores, nomes de arquivos ou decisões técnicas que você consegue resolver com segurança.
O Endeson pode sugerir ajustes no mesmo grupo. Incorpore os ajustes compatíveis com o escopo, informando quando alguma mudança ameaçar os requisitos P0.
Não prometa uma entrega concluída até ela realmente estar validada.
14. CHECKPOINTS DE EXECUÇÃO
Registrar o progresso em CHECKLIST.md com os estados:
Pendente.
Em andamento.
Concluído e testado.
Bloqueado.
Ao concluir cada fase, atualizar o arquivo e registrar:
O que foi implementado.
Quais testes foram executados.
O resultado.
O próximo passo.
Eventual impedimento.
Não transformar os checkpoints em solicitações de aprovação.
A execução deve continuar automaticamente enquanto existirem tarefas autorizadas e viáveis.
15. ENTREGA FINAL OBRIGATÓRIA
Quando finalizar, enviar ao grupo um relatório objetivo contendo:
PROJETO: Clínica-Escola UNINASSAU
Status: concluído, parcialmente concluído ou bloqueado.
1. Sistema publicado
Link real da aplicação Vercel e status do deployment.
2. Código-fonte
Link do repositório GitHub, branch e commit final.
3. Banco de dados
Provedor, status da conexão, tabelas criadas e evidência de persistência. Não divulgar credenciais.
4. Funcionalidades
Login: status.
Pacientes: status.
Especialidades: status.
Atendimentos: status.
5. Validação
Resumo dos testes executados e seus resultados.
6. Execução local
Como abrir no IntelliJ e iniciar a aplicação.
7. Evidências
Capturas de tela e resultados técnicos relevantes.
8. Pendências
Indicar claramente qualquer funcionalidade, teste ou requisito não concluído.
Não inventar links, resultados de testes ou informações de deploy.
16. COMANDO FINAL DE EXECUÇÃO
A.N.A., este PRD constitui a instrução de execução para a entrega acadêmica do Endeson.
Assuma a responsabilidade técnica pelo projeto, do planejamento à validação final, sem delegar para outros agentes ou CLIs de IA.
Seu foco é entregar, ainda hoje, as quatro funcionalidades solicitadas pelo professor em um projeto Java/Spring Boot com PostgreSQL, frontend HTML/CSS/JavaScript, código publicado no GitHub e aplicação acessível pela Vercel.
Não se perca em arquitetura, abstrações ou refinamentos desnecessários.
Ordem de prioridade: funcionamento real → banco persistente → integração → deploy → evidências → acabamento.
Comece imediatamente pela Fase 0, crie a pasta projeto rapido pro endeson, registre as tarefas e prossiga pela sequência de implementação.
Não interprete a criação do checklist como conclusão da missão. A missão só termina com a entrega funcional testada ou com a documentação transparente dos bloqueios que efetivamente impediram sua conclusão.
INICIAR EXECUÇÃO.