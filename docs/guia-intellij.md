# Guia: abrir e rodar no IntelliJ IDEA

1. **Abrir o projeto:** `File > Open...` e selecione o arquivo `pom.xml`. Escolha **Open as Project**. O IntelliJ reconhece o projeto Maven e baixa as dependências.
2. **JDK 21:** `File > Project Structure > Project > SDK`, selecione um JDK 21 (se não tiver, use *Download JDK* e escolha uma distribuição 21, como Temurin).
3. **Banco:** tenha um PostgreSQL rodando (veja o README) e um banco vazio criado.
4. **Variáveis de ambiente:** abra `src/main/java/br/edu/clinicaescola/ClinicaEscolaApplication.java`, clique no ▶ ao lado da classe e depois em `Modify Run Configuration...`. Em **Environment variables**, cole (ajustando os valores):
   ```
   JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/clinica;DB_USERNAME=clinica;DB_PASSWORD=clinica;DEMO_ADMIN_EMAIL=admin@clinica-escola.demo;DEMO_ADMIN_PASSWORD=uma-senha-forte
   ```
5. **Rodar:** clique em ▶ Run. Quando aparecer `Started ClinicaEscolaApplication`, acesse http://localhost:8080.
6. **Testes:** clique com o botão direito em `src/test/java` e escolha `Run 'All Tests'`. Para o teste de integração, adicione as mesmas variáveis `JDBC_DATABASE_URL`, `DB_USERNAME` e `DB_PASSWORD` na configuração de teste.

Observação: "Extension Pack for Java" e "Spring Boot Extension Pack" são extensões do **VS Code**. No IntelliJ não é preciso instalar nada além do JDK; o suporte a Maven e Spring já vem embutido.
