"""Visual v2: acceptance against real application, no exposed credentials."""
import argparse,json,os
from pathlib import Path
from playwright.sync_api import sync_playwright,expect
ap=argparse.ArgumentParser();ap.add_argument('--base',required=True);ap.add_argument('--prefix',default='visual-v2');ap.add_argument('--access-json',required=True);args=ap.parse_args()
root=Path(os.environ['PORTAL_QA_ROOT']);out=root/args.prefix;out.mkdir(exist_ok=True);base=args.base.rstrip('/');env=dict(l.split('=',1) for l in (root/'homolog.env').read_text().splitlines() if '=' in l);users=json.loads(Path(args.access_json).read_text());checks=[];errors=[]
def ok(s):checks.append(s);print('OK',s,flush=True)
def watch(p):p.on('pageerror',lambda e:errors.append(str(e)));return p
def ready(p):expect(p.locator('#conteudo')).to_have_attribute('aria-busy','false');expect(p.locator('.portal-hero')).to_be_visible()
def login(p,email,password):
 p.goto(base+'/',wait_until='networkidle');p.locator('#unidade').select_option('1');p.locator('#email').fill(email);p.locator('#senha').fill(password);p.get_by_role('button',name='Entrar no portal',exact=True).click();p.wait_for_url('**/portal.html');ready(p)
def overflow(p):assert p.evaluate('document.documentElement.scrollWidth <= innerWidth + 1')
def settle(p):
 p.evaluate('''async () => {
  await document.fonts.ready;
  await Promise.all(Array.from(document.images).map(async image => {
   image.loading = 'eager';
   await image.decode();
  }));
  await Promise.all(document.getAnimations().filter(a => a.effect?.getTiming().iterations !== Infinity).map(a => a.finished.catch(() => {})));
 }''')
def capture(p,**kwargs):
 settle(p)
 p.screenshot(**kwargs)
try:
 with sync_playwright() as pw:
  b=pw.chromium.launch(executable_path='/usr/bin/google-chrome',headless=True,args=['--no-sandbox']);c=b.new_context(viewport={'width':1440,'height':1000});p=watch(c.new_page());p.goto(base+'/',wait_until='networkidle')
  expect(p.get_by_role('tab',name='Aprender',exact=True)).to_have_attribute('aria-selected','true');p.get_by_role('tab',name='Acompanhar',exact=True).click();expect(p.get_by_role('tabpanel')).to_contain_text('Cada etapa, no seu tempo.');p.keyboard.press('ArrowRight');expect(p.get_by_role('tab',name='Organizar',exact=True)).to_be_focused();p.keyboard.press('Home');expect(p.get_by_role('tab',name='Aprender',exact=True)).to_be_focused();p.locator('.signin-box h2').click();overflow(p);capture(p,path=str(out/'login-desktop.png'),full_page=True);ok('Abas de login por clique e teclado, sem overflow desktop')
  login(p,env['PORTAL_ADMIN_EMAIL'],env['PORTAL_ADMIN_PASSWORD']);motion=p.locator('.portal-hero').evaluate('''async e=>{const a=e.getAnimations()[0];if(!a)return null;const before=Number(getComputedStyle(e).opacity);await a.finished;return {before,after:Number(getComputedStyle(e).opacity)}}''');assert motion and motion['before']<motion['after'] and motion['after']==1;ok('Animação real evolui entre quadros e termina visível');expect(p.locator('.quick-link')).to_have_count(4);p.locator('.quick-link[href="#cursos"]').click();expect(p.get_by_role('heading',name='Cursos',exact=True)).to_be_visible();p.goto(base+'/portal.html');ready(p);ok('Atalho do administrador abre módulo real')
  p.get_by_role('button',name='Buscar módulo',exact=True).click();expect(p.get_by_label('Buscar por nome do módulo')).to_be_focused();p.get_by_label('Buscar por nome do módulo').fill('MATRICULAS');expect(p.locator('.module-result')).to_have_count(1);p.keyboard.press('Enter');expect(p.get_by_role('heading',name='Matrículas',exact=True)).to_be_visible();expect(p.locator('#module-dialog')).not_to_be_visible();ok('Busca sem acentos, teclado Enter e navegação real')
  p.keyboard.press('Control+k');expect(p.locator('#module-dialog')).to_be_visible();p.get_by_label('Buscar por nome do módulo').fill('zzzinexistente');expect(p.locator('#module-empty')).to_be_visible();p.keyboard.press('Escape');expect(p.get_by_role('button',name='Buscar módulo',exact=True)).to_be_focused();ok('Ctrl+K, estado vazio, Escape e retorno de foco')
  p.goto(base+'/portal.html');ready(p);p.get_by_role('button',name='Próxima imagem',exact=True).click();expect(p.locator('#gallery-caption')).to_contain_text('Conhecimento que vira prática.');p.get_by_role('button',name='Ver imagem 3',exact=True).click();expect(p.locator('#gallery-caption')).to_contain_text('UNINASSAU Day');expect(p.get_by_role('link',name='Ver a fonte pública')).to_have_attribute('href','https://conheca.sereducacional.com/uninassauday');p.locator('.campaign-mark').scroll_into_view_if_needed();assert p.locator('.campaign-mark').evaluate('(e)=>e.complete && e.naturalWidth>0 && e.clientHeight<80');p.locator('.gallery').screenshot(path=str(out/'galeria-uninassau.png'));ok('Galeria manual, marca sem distorção e fonte institucional clicável')
  p.get_by_role('button',name='Imagem anterior',exact=True).click();expect(p.get_by_role('button',name='Ver imagem 2',exact=True)).to_have_attribute('aria-pressed','true');ok('Setas e indicadores da galeria sincronizados')
  p.get_by_role('button',name='Pausar animações',exact=True).click();expect(p.locator('body')).to_have_class('motion-off');p.reload();ready(p);expect(p.get_by_role('button',name='Ativar animações',exact=True)).to_have_attribute('aria-pressed','true');p.get_by_role('button',name='Ativar animações',exact=True).click();ok('Pausa de movimento persiste entre recargas')
  p.get_by_role('button',name='Explorar meus atalhos').click();expect(p.locator('#atalhos')).to_be_focused();p.evaluate('scrollTo(0,0)');capture(p,path=str(out/'painel-desktop.png'),full_page=True);overflow(p);ok('Explorar atalhos rola e transfere foco; painel sem overflow')
  for width in [320,390,768,1024]:
   p.set_viewport_size({'width':width,'height':900});overflow(p)
   if width==390:capture(p,path=str(out/'painel-mobile.png'),full_page=True)
  ok('Painel sem overflow a 320, 390, 768 e 1024 pixels')
  mc=b.new_context(viewport={'width':390,'height':844},is_mobile=True,has_touch=True);s=watch(mc.new_page());s.goto(base+'/',wait_until='networkidle');overflow(s);s.screenshot(path=str(out/'login-mobile.png'),full_page=True);login(s,users['ALUNO']['email'],users['ALUNO']['password']);expect(s.locator('.quick-link[href="#cursos"]')).to_have_count(0);s.get_by_role('button',name='Buscar módulo',exact=True).click();s.get_by_label('Buscar por nome do módulo').fill('cursos');expect(s.locator('#module-empty')).to_be_visible();s.get_by_label('Buscar por nome do módulo').fill('aulas');expect(s.locator('.module-result')).to_have_count(1);s.locator('.module-result').click();expect(s.get_by_role('heading',name='Aulas e materiais',exact=True)).to_be_visible();overflow(s);ok('Mobile: busca respeita perfil do aluno e abre aulas reais')
  rc=b.new_context(viewport={'width':1280,'height':900},reduced_motion='reduce');r=watch(rc.new_page());login(r,env['PORTAL_ADMIN_EMAIL'],env['PORTAL_ADMIN_PASSWORD']);expect(r.get_by_role('button',name='Movimento reduzido pelo sistema')).to_be_disabled();assert r.locator('.portal-hero').evaluate('(e)=>getComputedStyle(e).animationName')=='none';ok('Preferência do sistema desativa animações sem opção enganosa')
  for page in [p,s,r]:
   assert page.evaluate('Array.from(document.images).filter(i=>!i.loading || i.loading!=="lazy").every(i=>i.complete && i.naturalWidth>0)')
  assert not errors,errors;ok('Imagens principais carregadas e zero erros JavaScript');b.close()
 result={'success':True,'checks':checks,'errors':errors,'base':base};(out/'visual-results.json').write_text(json.dumps(result,indent=2,ensure_ascii=False));print(json.dumps({'success':True,'checks':len(checks),'errors':len(errors)}))
except Exception:
 (out/'visual-results.json').write_text(json.dumps({'success':False,'checks':checks,'errors':errors},indent=2));raise
