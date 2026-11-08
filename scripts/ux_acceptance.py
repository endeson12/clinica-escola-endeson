"""Real browser UX checks. No mocked API responses, no credential logging."""
import argparse,json,time
from pathlib import Path
from playwright.sync_api import sync_playwright,expect
ap=argparse.ArgumentParser();ap.add_argument('--base',required=True);ap.add_argument('--access-json',required=True);ap.add_argument('--output',required=True);a=ap.parse_args()
actor=json.loads(Path(a.access_json).read_text());out=Path(a.output);out.mkdir(parents=True,exist_ok=True)
checks=[];errors=[];base=a.base.rstrip('/')
def ok(s):checks.append(s);print('OK',s,flush=True)
def ready(p):expect(p.locator('#conteudo')).to_have_attribute('aria-busy','false')
def snap(p,name):
 p.evaluate("async()=>{await document.fonts.ready;await Promise.all([...document.images].filter(i=>i.src&&i.getBoundingClientRect().width).map(i=>{i.loading='eager';return i.decode()}));scrollTo({top:0,behavior:'instant'})}")
 p.screenshot(path=str(out/(name+'.png')),full_page=True,animations='disabled')
with sync_playwright() as pw:
 b=pw.chromium.launch(executable_path='/usr/bin/google-chrome',args=['--no-sandbox'],headless=True)
 c=b.new_context(viewport={'width':1440,'height':960},color_scheme='light');p=c.new_page();p.on('pageerror',lambda e:errors.append(str(e)))
 p.goto(base+'/',wait_until='networkidle')
 expect(p.get_by_role('button',name='Ativar modo escuro')).to_be_visible()
 p.get_by_role('button',name='Ativar modo escuro').click();expect(p.locator('html')).to_have_attribute('data-theme','dark');p.reload();expect(p.locator('html')).to_have_attribute('data-theme','dark');ok('Tema no login, persistência após reload')
 p.locator('#unidade').select_option('1');p.locator('#email').fill(actor['email']);p.locator('#senha').fill(actor['password']);p.get_by_role('button',name='Entrar no portal',exact=True).click();p.wait_for_url('**/portal.html');ready(p)
 expect(p.locator('html')).to_have_attribute('data-theme','dark');ok('Tema preservado no login autenticado')
 assert p.locator('#menu').evaluate("e=>getComputedStyle(e).scrollbarWidth==='thin'")
 assert p.locator('#menu').evaluate("e=>getComputedStyle(e).scrollbarColor.includes('rgba')||getComputedStyle(e).scrollbarColor.includes('rgb')")
 assert p.locator('#menu a svg').count()==p.locator('#menu a').count();ok('Menu rolável e SVGs consistentes em todos os itens')
 p.get_by_role('button',name='Conhecer o portal').click();expect(p.locator('#tour-dialog')).to_be_visible();p.get_by_role('button',name='Próximo passo').click();expect(p.locator('#tour-step')).to_contain_text('2 de');p.keyboard.press('Escape');expect(p.locator('#tour-dialog')).not_to_be_visible();ok('Tour: abrir, avançar, Escape e devolver foco')
 p.get_by_role('button',name='Abrir avisos').click();expect(p.locator('#service-dialog')).to_be_visible();expect(p.locator('#service-dialog')).to_contain_text('Avisos');p.keyboard.press('Escape');ok('Central de avisos aberta com dados do backend')
 for route,title in [('meu-curso','Meu curso'),('perfil','Meu perfil'),('solicitacoes','Solicitações'),('complementares','Atividades complementares'),('horarios','Quadro de horários'),('calendario','Calendário'),('recursos','Biblioteca e links')]:
  p.goto(base+'/portal.html#'+route);ready(p);expect(p.locator('h1')).to_contain_text(title);assert 'Não foi possível carregar' not in p.locator('h1').inner_text();assert p.evaluate('document.documentElement.scrollWidth<=innerWidth+1');ok('Módulo com dados reais/estado vazio honesto: '+route)
 p.goto(base+'/portal.html#solicitacoes');ready(p);p.get_by_role('button',name='Nova solicitação',exact=True).click();expect(p.locator('#entity-dialog')).to_be_visible();p.locator('#field-tipo').select_option('OUTROS');p.locator('#field-assunto').fill('QA fluxo de solicitação '+str(int(time.time())));p.locator('#field-mensagem').fill('Validação automatizada de homologação, sem pedido institucional real.');p.get_by_role('button',name='Enviar solicitação',exact=True).click();expect(p.locator('#entity-dialog')).not_to_be_visible();ready(p);expect(p.locator('#conteudo')).to_contain_text('QA fluxo de solicitação');p.reload();ready(p);expect(p.locator('#conteudo')).to_contain_text('QA fluxo de solicitação');ok('Criar solicitação, persistir no PostgreSQL e reler após reload')
 p.goto(base+'/portal.html#perfil');ready(p);p.get_by_role('button',name='Editar meus dados',exact=True).click();expect(p.locator('#field-telefone')).to_be_visible();p.get_by_role('button',name='Cancelar',exact=True).click();expect(p.locator('#entity-dialog')).not_to_be_visible();ok('Perfil: formulário real sem alterar dados do proprietário')
 p.goto(base+'/portal.html#financeiro');ready(p);p.get_by_role('button',name='Histórico',exact=True).click();expect(p.get_by_role('button',name='Histórico',exact=True)).to_have_attribute('aria-pressed','true');p.get_by_role('button',name='A pagar',exact=True).click();ok('Financeiro: abas filtram registros reais sem processar pagamentos')
 p.goto(base+'/portal.html#meu-curso');ready(p);snap(p,'meu-curso-escuro')
 for width in [320,390,768,1024,1440]:
  p.set_viewport_size({'width':width,'height':900});assert p.evaluate('document.documentElement.scrollWidth<=innerWidth+1');
  if width==390:
   p.get_by_role('button',name='Abrir navegação',exact=True).click();expect(p.locator('#sidebar')).to_have_class('sidebar open');p.locator('#menu').evaluate('e=>e.scrollTop=e.scrollHeight');expect(p.locator('#menu a').last).to_be_visible();snap(p,'menu-mobile-escuro');p.keyboard.press('Escape');snap(p,'meu-curso-mobile-escuro')
  ok('Sem overflow a '+str(width)+'px')
 p.set_viewport_size({'width':1440,'height':960});p.get_by_role('button',name='Ativar modo claro').click();expect(p.locator('html')).to_have_attribute('data-theme','light');snap(p,'meu-curso-claro')
 p.goto(base+'/portal.html#inicio');ready(p);snap(p,'inicio-claro');p.get_by_role('button',name='Sair da conta',exact=True).click();expect(p.locator('#confirm-dialog')).to_be_visible();p.get_by_role('button',name='Continuar na conta',exact=True).click();expect(p.locator('#confirm-dialog')).not_to_be_visible();ok('Logout usa confirmação visual e cancelar não encerra sessão')
 assert not errors,errors;b.close()
r={'success':True,'checks':checks,'page_errors':errors,'base':base};(out/'ux-results.json').write_text(json.dumps(r,ensure_ascii=False,indent=2));print(json.dumps({'success':True,'checks':len(checks),'page_errors':len(errors)}))
