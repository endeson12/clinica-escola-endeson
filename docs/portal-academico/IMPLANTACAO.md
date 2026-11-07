# Implantação e verificação do portal

## Artefatos e pré-requisitos

Java21, Maven3.9, PostgreSQL17, Docker/Compose para a implantação descrita. O JAR inclui frontend e backend. O site Vercel estático antigo **não** passa a executar Java/PostgreSQL; não tratá-lo como este ambiente.

```bash
# Aplicação: configurar banco próprio, nunca o de produção para testes.
mvn -B -ntp verify
# Gerar o pacote usado pelo Dockerfile.
mvn -B -ntp -DskipTests package
docker compose --env-file /caminho/privado/portal.env -f deploy/compose.yaml build
docker compose --env-file /caminho/privado/portal.env -f deploy/compose.yaml up -d
curl --fail http://127.0.0.1:18182/api/health
```

O Dockerfile copia `target/clinica-escola-0.0.1-SNAPSHOT.jar` já construído, usa JRE21 com digest fixo e executa como usuário10001. Compose aplica filesystem somente-leitura, `no-new-privileges`, capacidades removidas, limites de CPU/memória e PostgreSQL sem porta pública. O host expõe a aplicação **só em127.0.0.1**; publicar por reverse proxy HTTPS.

## Configuração privada

Criar arquivo de ambiente fora do Git, permissão0600. Não escrever credenciais em issues, screenshots, chat de grupo ou argumentos públicos. Variáveis esperadas:

- `PORTAL_DB_PASSWORD`: senha aleatória do PostgreSQL dedicado.
- `PORTAL_ADMIN_EMAIL` e `PORTAL_ADMIN_PASSWORD`: primeiro administrador.
- `PORTAL_RECEPCAO_EMAIL` e `PORTAL_RECEPCAO_PASSWORD`: recepção inicial.
- `PORTAL_BOOTSTRAP_ENABLED`: `true` somente na inicialização controlada, depois `false`. Não manter seed habilitado em operação: ele reconcilia as contas declaradas.
- `COOKIE_SECURE=true` atrás do HTTPS; `false` apenas em desenvolvimento HTTP isolado.
- `PORTAL_HTTP_PORT`:18182 por padrão, somente loopback.

Não há valores de senha padrão no Compose. A expansão recusa variáveis essenciais ausentes. Atenção: mudar `POSTGRES_PASSWORD` no arquivo não altera uma senha já persistida no volume; a rotação exige operação própria no banco e conferência de acesso.

O proxy deve preservar Host, enviar `X-Forwarded-Proto` e aceitar somente o upstream loopback. A aplicação já usa `server.forward-headers-strategy=framework`. Cookies de sessão são HttpOnly; CSRF permanece ativo, com token renovado após autenticação. Não usar `curl -k` para validar publicação.

## Testes de integração

Os testes são opt-in para impedir execução contra banco errado:

```bash
export INTEGRATION_TESTS_ENABLED=true
export JDBC_DATABASE_URL='jdbc:postgresql://127.0.0.1:5432/banco_exclusivo_de_testes'
export JDBC_DATABASE_USERNAME='usuario_de_teste'
# Carregar JDBC_DATABASE_PASSWORD de fonte privada, sem imprimir.
mvn -B -ntp verify
```

A verificação desta entrega usa PostgreSQL17 real e descartável, do V1 atéV4, nunca H2 ou respostas de API inventadas. A suíte contém23 testes: clínica/CSRF, revogação de sessão e13 testes acadêmicos/financeiros de fluxos e permissões. Sem `INTEGRATION_TESTS_ENABLED=true`, os testes de integração podem ser pulados; build verde sozinho não prova que eles rodaram. Conferir XML do Surefire, com zero testes pulados na validação efetiva.

## Aceitação em navegador

Instalar Playwright Python em ambiente próprio e usar Chrome/Chromium disponível. O script testa **e grava dados sintéticos identificados de homologação**; nunca executá-lo contra banco real.

```bash
# Diretório externo com homolog.env (variáveis de administrador descritas acima).
export PORTAL_QA_ROOT=/caminho/privado/evidencias
python scripts/browser_acceptance.py --base https://seu-ambiente-de-homologacao --prefix aceite
```

O executável Chrome padrão é `/usr/bin/google-chrome`. O script guarda apenas em `PORTAL_QA_ROOT`: resultados, screenshots, exportações demonstrativas e credenciais aleatórias dos usuários de teste em arquivo0600. Nenhum acesso é gravado no Git ou impresso. A execução exercita formulários, APIs reais, persistência, erro409, seis perfis, entrega, rascunho/publicação de nota, CSV, baixa simulada, carteirinha/PDF, falha de rede recuperável, versão mobile e logout. Uma assertion interrompe a execução; recibo parcial não é aceite.

## Backup e restauração

Antes de migração/operação destrutiva, gerar `pg_dump -Fc` em destino privado0600. Exemplo, com diretório previamente criado e protegido:

```bash
umask 077
docker compose --env-file /caminho/privado/portal.env -f deploy/compose.yaml exec -T db \
  pg_dump -U portal -d portal -Fc > /backup/portal-data.dump
# Conferir o arquivo e testar restauração em OUTRO banco, não na aplicação.
docker compose --env-file /caminho/privado/portal.env -f deploy/compose.yaml exec -T db \
  pg_restore --list < /backup/portal-data.dump
```

A listagem valida o formato, **não** prova restauração completa. Na entrega, o dump foi restaurado em banco temporário separado e conferido antes de remover a cópia temporária; o banco original foi preservado. Para produção: retenção, cópia externa cifrada, agenda, responsáveis e ensaio periódico de restauração precisam ser definidos. Nenhuma rotina de exclusão ou cron foi criada implicitamente.

## Atualização e rollback

Guardar dump, commit e imagem anterior antes de atualizar. Reconstruir o JAR, executar testes, construir nova imagem e recriar **só** o serviço da aplicação. Conferir health, recursos públicos byte a byte e jornada em HTTPS. Migrações são incrementais: voltar imagem não equivale a desfazer banco. Não executar `down -v` nem apagar volumes para corrigir uma implantação. Restauração do banco exige janela, autorização e teste em cópia.
