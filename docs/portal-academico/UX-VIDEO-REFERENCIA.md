# Refinamento UX a partir da gravação do proprietário

## Briefing / alvo de implementação
Portal acadêmico e clínica existentes, Java/PostgreSQL; não reconstruir backend. Marca e Manrope locais preservadas. Corrigir scrollbar branca do recorte enviado, ícones inconsistentes e ausência de tema noturno. Referência principal: vídeo enviado em Drive (207,1s, 1280×674), inspecionado localmente com watch e quadros adicionais em resolução original. Transcrição local retornou zero segmentos: a análise é visual, não uma transcrição de falas.

## Matriz de evidências
| Tempo | Observação visual | Adaptação e limite |
|---|---|---|
| 00:00–00:17 | Login, identificação e abertura do portal | Preservar autenticação própria, sem capturar credenciais do vídeo |
| 00:17–00:43; 01:20–01:48 | Meu curso, disciplinas, semestre, professor, materiais e avaliações | Visão por período usando turmas/matrículas/aulas/notas reais do banco; não inventar frequência ou aprovação institucional |
| 00:43; 01:40 | Calendário e acesso a eventos | Calendário de prazos publicados no portal, sem presumir integração Outlook |
| 00:58–01:03 | Grade semanal, dias/horários/salas e menu de solicitações | Cadastro administrativo de horários por turma; leitura respeita vínculo e unidade |
| 01:03 | Links úteis e serviços Office | Catálogo local de materiais publicados; não simular SSO Office |
| 01:11–01:16 | Meu perfil: contato, endereço, escolaridade, salvar/cancelar | Formulário persistido por usuário/unidade; identidade e perfil de acesso não são autoeditáveis |
| 01:27–01:48 | Troca entre períodos e notas anteriores | Filtro de períodos reais; sem criar semestres fictícios |
| 02:03–02:15 | Complementares: certificado, data, status, horas enviadas/aprovadas e adicionar | Submissão com referência HTTPS, revisão por gestão e total aprovado; sem upload binário nesta entrega |
| 02:17 | Botão iniciar passeio, sino de avisos e perfil | Tour manual e central de avisos derivados dos registros reais; não forçar popup promocional |
| 02:28–02:55 | AVA externo em modo escuro, avisos, avaliações, biblioteca e filtros | Tema claro/escuro próprio e materiais locais; integração AVA/provas externas não presumida |
| 03:02–03:13 | Pagamentos: A pagar/Histórico, busca e detalhes | Abas e detalhamento dos lançamentos demonstrativos; sem Pix, cartão, boleto ou negociação real |
| 03:20–03:25 | Menu solicitações: documentos, mudança de turma/turno, certificados | Solicitações internas protocoladas, resposta administrativa e cancelamento pelo titular; não emitem documentos oficiais |

## Referências e decisões (reference lock)
Sem Refero MCP disponível: referência aprovada do vídeo + craft `refero-design/references/icons.md`, `color.md`; processo `REEL-DeakyY6z51c.md`. Preservar azul institucional e dourado no brasão/hero; usar superfícies neutras distintas em modo escuro, não filtro invert. SVG de traço único/currentColor para interface; 20px/1,7px e hit area móvel ≥44px. Menu com rolagem fina, fundo integrado e todos os itens alcançáveis. Modais nativos dialog, Escape, foco restaurado, cancelamento e erros reais. Tema em storage apenas de preferência não sensível, inicializado por script externo antes do CSS para evitar clarão. Sem redesenho de marca nem bibliotecas remotas.

## Segurança / produção
Backup Git e tag Docker before-ux-refinement preservados. Migração aditiva, testes RED/GREEN em PostgreSQL separado. Novos serviços sempre resolvem identidade da sessão, respeitam unidade e perfil, CSRF e auditoria sem conteúdo pessoal. Relatório/capturas do vídeo ficam privados fora do Git; nenhum dado pessoal do vídeo é copiado para o banco ou documentação. Homologação e natureza demonstrativa mantidas em financeiro e carteirinha.
