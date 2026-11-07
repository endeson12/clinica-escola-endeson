# Imagens e identidade visual — versão 2

## Direção

Azul universitário `#154fce`, azul profundo `#092866`, amarelo-luz `#ffd241`, branco e superfícies claras. Tipografia Manrope local com licença OFL existente. A assinatura visual é a fotografia conceitual de ensino e cuidado, com interfaces funcionais discretas ao redor. Não simular vínculo oficial.

## Artes originais

- `campus-conceitual.webp` e variante `-640`: criadas via GPT Image 2 para esta entrega. Cena universitária **conceitual**, não fotografia de campus real. Não há logos ou nomes de instituição na geração.
- `estudo-saude.webp` e variante `-640`: composição original de materiais de estudo e saúde, gerada via GPT Image 2.
- WebP com dimensões declaradas, variantes responsivas e lazy loading no conteúdo secundário. Sem dependência externa no carregamento das imagens.

## Referências públicas UNINASSAU

Fonte: <https://conheca.sereducacional.com/uninassauday>, página pública do Grupo Ser Educacional. Acesso autorizado pelo usuário para esta homologação.

- `uninassau-campanha.webp`: imagem de campanha pública, original `https://cdn.greatpages.com.br/conheca.sereducacional.com-uninassauday/1679668144/imagens/desktop/9358758.png`.
- `uninassau-referencia.webp`: marca da fonte exibida **somente no slide de referência**, original `https://cdn.greatpages.com.br/conheca.sereducacional.com-uninassauday/1679668144/imagens/desktop/9358839.png`.
- Conversão WebP sem perdas. Não atribuir licença aberta, autoria do portal ou cessão de direitos a essas imagens. Marcas e imagens permanecem de seus respectivos titulares. Fonte clicável na galeria; a imagem não anuncia evento atual nem representa aluno cadastrado.
- Validar direitos de uso antes de publicação institucional/comercial. Não transformar a marca da fonte no logotipo do portal nem sugerir que este ambiente é um serviço oficial da instituição.
- As páginas de notícias retornaram 403, e o blog não resolveu DNS. Não foi contornado controle de acesso. Não apresentar fotos de outras cidades como se fossem de Teresina.

## Interações

- Busca de módulos por nome, Ctrl/Cmd+K, acesso somente aos módulos permitidos ao perfil e fechamento por Escape.
- Atalhos por perfil para rotas reais; sem botões que simulam integração.
- Galeria manual, sem autoplay, com botões, indicadores e anúncio acessível.
- Abas de apresentação do login com teclado (setas, Home/End).
- Entrada suave e microinterações; `prefers-reduced-motion` respeitado e opção persistente para pausar movimentos não essenciais. Nenhum contador, notificação ou número operacional inventado.

## Critérios de acabamento aplicados

- `design-skills-workflow` e processo `REEL-DeakyY6z51c`: referência visual, produção e inspeção de capturas reais desktop/mobile.
- Emil `review-animations/STANDARDS.md`: paleta de comandos imediata; entradas discretas de 280 ms; movimento desativável e respeito ao sistema.
- Impeccable `polish.md`: corrigir foco/fluxo antes do acabamento; preservar comportamento e permissões existentes. Sem instalar ou executar engine/hooks.
- Taste `0.A / 0.D`: linguagem editorial no login, não aplicar receitas de landing pages às tabelas operacionais. Preservados Manrope, marca do portal e stack Java/JS existente.
- Sem Figma conectado: composição construída e conferida diretamente no Chrome/Playwright, sem alegação de arquivo Figma.

## Verificação reproduzível

- Regressão Java/PostgreSQL executada nesta frente: 23 testes, zero falhas, erros ou ignorados.
- `scripts/visual_acceptance.py`: 13 verificações locais, incluindo movimento real observado entre quadros, navegação por teclado, galeria e responsividade de 320 a 1440 px.
- Para regressão funcional integral, executar `scripts/browser_acceptance.py` contra HTTPS real. Um preview HTTP com proxy não é equivalente: o navegador pode aceitar cookies Secure em loopback, enquanto o cliente HTTP de API do Playwright não os reenvia. Não enfraquecer cookies para acomodar o teste.
- Exemplo, com arquivos privados fora do repositório: `PORTAL_QA_ROOT=/caminho/privado python scripts/visual_acceptance.py --base https://seu-host --access-json /caminho/privado/qa-access.json --prefix visual-v2`.

Execução/autoria: **LEM / assistência de IA autorizada**. Frente frontend de Leônidas; publicação designada `ldevloper`. Não implica desenvolvimento manual ou revisão humana.
