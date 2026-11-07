# Portal institucional — guia de operação

Projeto independente. Este ambiente **não se conecta a sistemas oficiais de nenhuma instituição** e não deve receber dados pessoais reais na homologação. O domínio clínico e o acadêmico são separados; não transformar pacientes em alunos por inferência.

## Acessar e administrar

1. Abra a URL HTTPS do ambiente e selecione a unidade.
2. Use o e-mail e a senha fornecidos por canal privado. Não existe cadastro público nem escolha de perfil no login.
3. A administração cria os demais acessos em **Pessoas e acessos**. A senha inicial tem ao menos 12 caracteres, até 64 caracteres e no máximo 72 bytes UTF-8. Não compartilhar acessos de administrador.
4. Para trocar a unidade, encerre a sessão e entre novamente na unidade desejada.
5. **Sair da conta** encerra a sessão também no servidor. Desativar usuário ou remover o vínculo revoga sessões existentes.

## Jornada administrativa

- **Cursos:** cadastrar código, nome e carga horária; editar dados.
- **Disciplinas:** vincular ao curso; editar código, nome e carga horária.
- **Pessoas:** cadastrar aluno/professor e demais perfis; gerenciar nome, perfil e situação. A coordenação não cria administradores ou perfis financeiros. Contas multiunidade têm proteção contra suspensão global indevida.
- **Turmas:** vincular disciplina, professor ativo, período e número de vagas; editar dentro da unidade.
- **Matrículas:** selecionar turma e aluno ativo; unicidade e lotação são verificadas no banco. Cancelar uma matrícula retira acesso ao conteúdo e preserva o histórico.
- **Histórico de ações:** consultar as últimas 200 alterações auditadas na unidade. Não há API para editar ou apagar esta trilha.

As listas têm busca, paginação visual e limite explícito de 1.000 registros por consulta. O painel calcula indicadores dos dados persistidos, não usa métricas ilustrativas. A paginação de servidor para operação de grande volume não integra esta versão.

## Jornada do professor

1. Entre com perfil de professor. Só suas turmas e seus alunos ficam acessíveis.
2. Em **Aulas e materiais**, publique texto e, opcionalmente, um link HTTPS autorizado. Os links abrem em outra aba; o portal não hospeda arquivos nem verifica direitos de terceiros.
3. Em **Atividades**, publique enunciado e prazo no seu horário local.
4. Em **Entregas**, leia a resposta do aluno e registre uma avaliação.
5. Mantenha a publicação desmarcada para salvar como **rascunho**. Nota e feedback privados não aparecem para o aluno.
6. Marque **Publicar avaliação para o aluno** para disponibilizá-los no boletim.

Depois de receber entregas, enunciado e prazo da atividade ficam protegidos contra alteração. Reatribuir uma turma a outro docente é uma ação administrativa auditada.

## Jornada do aluno

- Consulte **Minha matrícula**, turmas, aulas e atividades.
- A entrega é texto, podendo conter uma URL. Confira e confirme o **envio final**: não é substituível; não há upload de binários nesta versão.
- Atividade fora do prazo não aceita envio. IDs de turmas/atividades alheias não concedem acesso.
- **Notas e boletim** mostra apenas avaliações publicadas, com exportação CSV demonstrativa.
- A escala operacional de homologação é individual de **0 a 10**, provisória e explícita. Não há média, aprovação, recuperação ou arredondamento institucional automático. A adoção institucional depende de validação dessas regras pelo responsável.
- **Financeiro** permite somente consultar o próprio extrato simulado.
- **Carteirinha** depende de matrícula ativa e sempre mostra **DEMONSTRAÇÃO · SEM VALIDADE OFICIAL**. É imprimível; não comprova meia-entrada nem autentica terceiros.

## Financeiro, coordenação e recepção

- **Financeiro:** consulta alunos da unidade, cadastra lançamentos demonstrativos e registra uma baixa demonstrativa com confirmação. Não gera Pix, boleto, débito, transferência ou comunicação de cobrança.
- **Coordenação:** gerencia cadastros e vínculos acadêmicos. Não herda acesso ao financeiro ou aos pacientes.
- **Recepção:** acessa a gestão clínica e não recebe acesso acadêmico amplo.
- **Administração:** tem operações privilegiadas, com CSRF, vínculo de unidade e auditoria; não há isenção de controle.

## Preservação da clínica

**Gestão clínica** mantém painel, pacientes, especialidades e atendimentos. Aluno, professor, coordenação e financeiro não podem ler pacientes nem por chamada direta à API. Os testes de login, CSRF, logout e isolamento clínico continuam ativos.

## Limites de homologação

Sem SSO oficial, emissão de documentos oficiais, pagamentos reais, videoconferência própria, importação institucional, upload de arquivos ou streaming. Sem promessa de conformidade assistencial, prontuário completo, SLA ou adequação institucional/LGPD apenas por existir código. Antes de dados reais: validar regras acadêmicas, política de acesso/retenção, monitoramento, recuperação de acesso, rotinas de backup e aceite institucional.

Para implantação, testes reproduzíveis e backup, consulte `IMPLANTACAO.md`.
