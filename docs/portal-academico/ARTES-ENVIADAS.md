# Artes enviadas e aplicação de marca

## Escopo
Solicitação do proprietário: usar as oito imagens fornecidas no portal e padronizar a marca em toda a interface.

- O brasão PNG enviado é a marca principal. Apenas a margem vazia foi reduzida; o desenho não foi redesenhado, recolorido nem esticado.
- Login, navegação do portal, cabeçalho mobile, quatro páginas clínicas e favicon usam o mesmo brasão.
- Impressão e carteirinha demonstrativa incluem a marca, sem remover os avisos de ausência de validade oficial.
- As oito imagens completas foram convertidas para WebP sem perdas e integradas à galeria manual, com legenda, texto alternativo e proporções próprias.
- As três artes/referências anteriores continuam disponíveis. Não existe avanço automático obrigatório.
- A interface preserva a indicação de homologação independente. Arquivos enviados não comprovam licença comercial nem vínculo institucional.

## Arquivos
`img/arte-enviada-01.webp` a `img/arte-enviada-08.webp` correspondem à ordem dos oito anexos. `img/uninassau-brasao.png` deriva do sétimo anexo; `favicon.ico` contém versões 16, 32, 48 e 64 px. CSS específico: `css/brand.css`.

## Verificação
- `scripts/brand_acceptance.py`: login HTTPS real, arquivos públicos byte a byte, oito artes navegáveis, marca nas quatro páginas clínicas, tamanhos de 320 a 1440 px, impressão demonstrativa e logout.
- `scripts/visual_acceptance.py`: atalhos, busca, teclado, galeria, animação real, movimento reduzido e perfil do aluno.
- Maven/PostgreSQL isolado: 23 testes, sem falhas ou erros.
- Backup Git `before-user-brand.bundle` e imagem de retorno `clinica-escola-portal:before-user-brand` preservados fora do repositório. Nenhuma migração ou alteração de credenciais nesta entrega.

## Autoria
Execução e autoria: LEM, assistência de IA autorizada. Frente de frontend atribuída a Leônidas; conta publicadora `ldevloper`. Não implica implementação manual ou revisão humana por Leônidas.
