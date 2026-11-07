"""Aceitação real via Playwright/Chrome; credenciais só em arquivo 0600 fora do repositório."""
import argparse, json, os, secrets, time, traceback
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from pathlib import Path
from playwright.sync_api import sync_playwright, expect

ap=argparse.ArgumentParser();ap.add_argument('--base',default='http://127.0.0.1:18182');ap.add_argument('--prefix',default='local');args=ap.parse_args()
BASE=args.base.rstrip('/');ROOT=Path(os.environ.get('PORTAL_QA_ROOT',str(Path(__file__).parent)));OUT=ROOT/args.prefix;OUT.mkdir(exist_ok=True)
env=dict(line.split('=',1) for line in (ROOT/'homolog.env').read_text().splitlines() if '=' in line)
tag=datetime.now().strftime('%m%d%H%M%S')+secrets.token_hex(2)
checks=[];errors=[];http_errors=[];expected_network=False;users={};pages=[]
def ok(name):checks.append(name);print('OK:',name,flush=True)
def report(success):
 data={'base':BASE,'tag':tag,'success':success,'checks':checks,'page_errors':errors,'unexpected_http':http_errors,'checked_at':datetime.now().astimezone().isoformat()}
 (OUT/'browser-results.json').write_text(json.dumps(data,ensure_ascii=False,indent=2));print(json.dumps({'success':success,'checks':len(checks),'page_errors':len(errors),'unexpected_http':len(http_errors)}),flush=True)
def private_users():
 f=OUT/'qa-access.json';f.write_text(json.dumps(users));f.chmod(0o600)
def watch(page):
 page.on('pageerror',lambda e:errors.append(str(e)))
 def resp(r):
  if r.status>=400 and not (r.status==401 and r.url.endswith('/api/auth/me')) and not (r.status==409 and r.request.method=='POST' and r.url.endswith('/academico/cursos')):
   http_errors.append({'status':r.status,'path':r.url.split(BASE)[-1].split('?')[0]})
 page.on('response',resp)
 return page

def ready(page):
 expect(page.locator('main')).to_have_attribute('aria-busy','false',timeout=20000)
 page.wait_for_function("() => window.innerWidth > 780 || document.querySelector('#sidebar').getBoundingClientRect().right <= 1")
def login(page,email,password):
 page.goto(BASE+'/',wait_until='networkidle');expect(page.locator('#email')).to_be_enabled();page.locator('#unidade').select_option('1')
 page.locator('#email').fill(email);page.locator('#senha').fill(password)
 with page.expect_response(lambda r:r.url.endswith('/api/auth/login') and r.request.method=='POST') as r:page.get_by_role('button',name='Entrar no portal').click()
 assert r.value.status==200
 page.wait_for_url('**/portal.html');ready(page)
def go(page,key,title):
 if page.url.split('#')[-1]==key:page.reload()
 else:page.locator(f'#menu a[href="#{key}"]').click()
 title={'Boletim de notas':'Notas e boletim','Extrato demonstrativo':'Financeiro','Auditoria':'Histórico de ações','Visão geral':'Visão geral do campus'}.get(title,title)
 expect(page.get_by_role('heading',name=title,exact=True)).to_be_visible();ready(page)
def field(page,label,value):
 expect(page.locator('#entity-dialog')).to_be_visible(timeout=20000)
 aliases={'Código':['Código do curso','Código da disciplina'],'Carga horária (h)':['Carga horária (horas)'],'E-mail':['E-mail de acesso'],'Número de vagas':['Vagas'],'Título da aula':['Título'],'Título da atividade':['Título'],'Instruções':['Enunciado e orientações'],'Prazo de entrega':['Prazo de entrega (seu horário local)'],'Link do material (HTTPS, opcional)':['Link HTTPS do material'],'Nota (0 a 10)':['Nota individual (0 a 10)'],'Devolutiva':['Feedback para o aluno'],'Descrição':['Descrição do lançamento demonstrativo'],'Valor demonstrativo (R$)':['Valor simulado (R$)']}
 for candidate in aliases.get(label,[label]):
  loc=page.get_by_label(candidate,exact=False)
  if loc.count()==1:loc.fill(str(value));return
 raise AssertionError('Campo não encontrado: '+label)
def select(page,label,value):page.get_by_label({'Perfil de acesso':'Perfil nesta unidade'}.get(label,label),exact=True).select_option(str(value))
def submit(page,path,method='POST',name='Salvar registro',status=201):
 with page.expect_response(lambda r:r.url.split('?')[0].endswith('/api/academico'+path) and r.request.method==method) as rr:
  page.get_by_role('button',name=name,exact=True).click()
 r=rr.value;assert r.status==status,(path,r.status,r.text()[:150]);body=r.json()
 if status<300:expect(page.locator('#entity-dialog')).not_to_be_visible();ready(page)
 return body

def row(page,text):return page.get_by_role('row').filter(has_text=text)
def no_overflow(page,label):
 assert page.evaluate('document.documentElement.scrollWidth <= document.documentElement.clientWidth + 1'),label
 ok('Sem overflow horizontal: '+label)

def new_person(page,role,label):
 go(page,'pessoas','Pessoas e acessos');page.get_by_role('button',name='Nova pessoa',exact=True).click()
 email=f'portal.{role.lower()}.{tag}@qa.invalid';password=secrets.token_urlsafe(16);name=label+' · '+tag
 field(page,'Nome completo',name);field(page,'E-mail',email);field(page,'Senha inicial',password);select(page,'Perfil de acesso',role)
 data=submit(page,'/pessoas');users[role]={'id':data['id'],'nome':name,'email':email,'password':password};private_users();page.get_by_role('searchbox',name='Buscar nesta lista').fill(name);expect(row(page,name)).to_be_visible();return data

try:
 with sync_playwright() as pw:
  browser=pw.chromium.launch(executable_path='/usr/bin/google-chrome',headless=True,args=['--no-sandbox'])
  admin_context=browser.new_context(viewport={'width':1440,'height':1000},timezone_id='America/Fortaleza');admin=watch(admin_context.new_page());pages.append(admin)
  admin.goto(BASE+'/',wait_until='networkidle');admin.screenshot(path=str(OUT/'login-desktop.png'),full_page=True)
  no_overflow(admin,'login desktop');login(admin,env['PORTAL_ADMIN_EMAIL'],env['PORTAL_ADMIN_PASSWORD']);ok('Login de administrador via formulário e sessão real')
  go(admin,'cursos','Cursos');name='Curso de Homologação · '+tag;code='QA-'+tag
  admin.get_by_role('button',name='Novo curso',exact=True).click();field(admin,'Código',code);field(admin,'Nome do curso',name);field(admin,'Carga horária (h)',40)
  curso=submit(admin,'/cursos');expect(row(admin,name)).to_be_visible();admin.reload();expect(row(admin,name)).to_be_visible();ok('Curso criado por formulário e persistente após recarga')
  row(admin,name).get_by_role('button',name='Editar',exact=True).click();field(admin,'Carga horária (h)',48);submit(admin,f'/cursos/{curso["id"]}','PUT',status=200);ok('Edição de curso integrada à API PUT')
  admin.get_by_role('button',name='Novo curso',exact=True).click();field(admin,'Código',code);field(admin,'Nome do curso','Duplicado de homologação');field(admin,'Carga horária (h)',40)
  submit(admin,'/cursos',status=409);expect(admin.locator('#form-error')).to_be_visible();admin.once('dialog',lambda d:d.accept());admin.get_by_role('button',name='Cancelar',exact=True).click();ok('Conflito real de código duplicado mostra erro recuperável no formulário')
  for role,label in [('PROFESSOR','Docente de Homologação'),('ALUNO','Aluno de Homologação'),('FINANCEIRO','Financeiro de Homologação'),('COORDENACAO','Coordenação de Homologação'),('RECEPCAO','Recepção de Homologação')]:new_person(admin,role,label)
  ok('Cinco perfis criados pela interface, sem conta pública ou atribuição de papel livre')
  go(admin,'disciplinas','Disciplinas');admin.get_by_role('button',name='Nova disciplina',exact=True).click();select(admin,'Curso',curso['id']);field(admin,'Código','D-'+tag);field(admin,'Nome da disciplina','Disciplina de Homologação · '+tag);field(admin,'Carga horária (h)',24)
  disc=submit(admin,'/disciplinas')
  go(admin,'turmas','Turmas');admin.get_by_role('button',name='Nova turma',exact=True).click();field(admin,'Nome da turma','Turma de Homologação · '+tag);field(admin,'Período letivo','Homologação');select(admin,'Disciplina',disc['id']);select(admin,'Professor responsável',users['PROFESSOR']['id']);field(admin,'Número de vagas',20)
  turma=submit(admin,'/turmas')
  go(admin,'matriculas','Matrículas');admin.get_by_role('button',name='Nova matrícula',exact=True).click();select(admin,'Turma',turma['id']);select(admin,'Aluno',users['ALUNO']['id']);matricula=submit(admin,'/matriculas');ok('Administrador vincula curso, disciplina, professor, turma e aluno por APIs reais')
  ctxp=browser.new_context(viewport={'width':1440,'height':1000},timezone_id='America/Fortaleza');prof=watch(ctxp.new_page());pages.append(prof);login(prof,users['PROFESSOR']['email'],users['PROFESSOR']['password'])
  expect(prof.locator('#menu a[href="#pessoas"]')).to_have_count(0);expect(prof.locator('#menu a[href="/dashboard.html"]')).to_have_count(0);assert ctxp.request.get(BASE+'/api/pacientes').status==403
  go(prof,'aulas','Aulas e materiais');prof.get_by_role('button',name='Publicar aula',exact=True).click();select(prof,'Turma',turma['id']);field(prof,'Título da aula','Orientações de Homologação · '+tag)
  xss='Conteúdo didático de homologação. Texto literal seguro: <img src=x onerror="window.__portalInjected=true">'
  field(prof,'Conteúdo da aula',xss);field(prof,'Link do material (HTTPS, opcional)','https://example.org/material-de-homologacao');aula=submit(prof,'/aulas');expect(prof.get_by_text(xss,exact=True)).to_be_visible();assert prof.evaluate('window.__portalInjected === undefined');assert prof.locator('img[src="x"]').count()==0
  ok('Professor publica aula e link HTTPS; HTML de entrada é texto, não execução de script')
  go(prof,'atividades','Atividades');prof.get_by_role('button',name='Nova atividade',exact=True).click();select(prof,'Turma',turma['id']);field(prof,'Título da atividade','Atividade de Homologação · '+tag);field(prof,'Instruções','Descreva o uso do portal no processo de ensino. Atividade exclusiva de homologação.')
  field(prof,'Prazo de entrega',(datetime.now(ZoneInfo('America/Fortaleza'))+timedelta(days=7)).strftime('%Y-%m-%dT%H:%M'))
  atividade=submit(prof,'/atividades');prof.screenshot(path=str(OUT/'professor-atividades.png'),full_page=True);ok('Professor publica atividade com prazo persistido')
  ctxa=browser.new_context(viewport={'width':390,'height':844},timezone_id='America/Fortaleza',is_mobile=True,has_touch=True);aluno=watch(ctxa.new_page());pages.append(aluno);login(aluno,users['ALUNO']['email'],users['ALUNO']['password']);no_overflow(aluno,'início do aluno a 390px')
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.locator('#menu a[href="#aulas"]').click();ready(aluno);expect(aluno.get_by_text(xss,exact=True)).to_be_visible();assert aluno.evaluate('window.__portalInjected === undefined');ok('Aluno matriculado lê somente o conteúdo atribuído, com texto seguro')
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.locator('#menu a[href="#atividades"]').click();ready(aluno);aluno.get_by_role('button',name='Enviar resposta',exact=True).click();field(aluno,'Sua resposta final','Entrega real de aceitação em navegador. Ambiente de homologação, sem dado pessoal real.');aluno.get_by_label('Conferi minha resposta e confirmo o envio final.').check()
  entrega=submit(aluno,'/entregas',name='Confirmar envio final');expect(aluno.get_by_role('button',name='Enviado',exact=True)).to_be_disabled();aluno.screenshot(path=str(OUT/'aluno-atividade-mobile.png'),full_page=True);no_overflow(aluno,'atividade entregue a 390px');ok('Aluno envia resposta pelo celular; entrega final fica imutável')
  go(prof,'entregas','Entregas');row(prof,users['ALUNO']['nome']).get_by_role('button',name='Ler',exact=True).click();expect(prof.get_by_text('Entrega real de aceitação em navegador. Ambiente de homologação, sem dado pessoal real.',exact=True)).to_be_visible();prof.get_by_role('button',name='Fechar formulário').click()
  row(prof,users['ALUNO']['nome']).get_by_role('button',name='Avaliar').click();field(prof,'Nota (0 a 10)','8.50');field(prof,'Devolutiva','Rascunho privado de avaliação.');submit(prof,f'/entregas/{entrega["id"]}/avaliacao','PUT',name='Salvar avaliação',status=200)
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.locator('#menu a[href="#boletim"]').click();ready(aluno);expect(aluno.get_by_role('heading',name='Ainda não há registros.')).to_be_visible();ok('Rascunho de nota e devolutiva não é revelado ao aluno')
  row(prof,users['ALUNO']['nome']).get_by_role('button',name='Avaliar').click();field(prof,'Devolutiva','Resposta avaliada em homologação.');prof.get_by_label('Publicar avaliação para o aluno. Desmarque para manter como rascunho.').check();submit(prof,f'/entregas/{entrega["id"]}/avaliacao','PUT',name='Salvar avaliação',status=200)
  aluno.reload();ready(aluno);expect(aluno.get_by_text('8,5',exact=True)).to_be_visible();expect(aluno.get_by_text('Resposta avaliada em homologação.',exact=True)).to_be_visible();aluno.screenshot(path=str(OUT/'aluno-boletim-mobile.png'),full_page=True)
  with aluno.expect_download() as download:aluno.get_by_role('button',name='Exportar notas CSV').click()
  csv=OUT/'boletim-demonstrativo.csv';download.value.save_as(str(csv));assert 'DEMONSTRATIVO' in csv.read_text(encoding='utf-8-sig');ok('Nota publicada aparece no boletim; exportação CSV funciona e é demonstrativa')
  ctxf=browser.new_context(viewport={'width':1440,'height':1000},timezone_id='America/Fortaleza');fin=watch(ctxf.new_page());pages.append(fin);login(fin,users['FINANCEIRO']['email'],users['FINANCEIRO']['password']);go(fin,'financeiro','Extrato demonstrativo');fin.get_by_role('button',name='Novo lançamento',exact=True).click();select(fin,'Aluno',users['ALUNO']['id']);field(fin,'Descrição','Lançamento de Homologação · '+tag);field(fin,'Valor demonstrativo (R$)','125.50');field(fin,'Vencimento',(datetime.now()+timedelta(days=15)).strftime('%Y-%m-%d'));lancamento=submit(fin,'/financeiro')
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.locator('#menu a[href="#financeiro"]').click();ready(aluno);expect(aluno.get_by_text('125,50',exact=False).first).to_be_visible();no_overflow(aluno,'financeiro a 390px');assert aluno.get_by_role('button',name='Simular baixa').count()==0
  fin.once('dialog',lambda d:d.accept());row(fin,'Lançamento de Homologação · '+tag).get_by_role('button',name='Simular baixa').click();expect(row(fin,'Lançamento de Homologação · '+tag).get_by_text('Baixa demonstrativa')).to_be_visible();aluno.reload();ready(aluno);expect(aluno.get_by_text('Baixa demonstrativa',exact=True)).to_be_visible();ok('Financeiro registra e simula baixa; aluno somente consulta; nenhum pagamento é acionado')
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.locator('#menu a[href="#carteirinha"]').click();ready(aluno);expect(aluno.get_by_text('DEMONSTRAÇÃO · SEM VALIDADE OFICIAL',exact=True)).to_be_visible();expect(aluno.get_by_role('heading',name=users['ALUNO']['nome'],exact=True)).to_be_visible();aluno.screenshot(path=str(OUT/'carteirinha-mobile.png'),full_page=True);no_overflow(aluno,'carteirinha a 390px');aluno.pdf(path=str(OUT/'carteirinha-demonstrativa.pdf'),print_background=True);ok('Carteirinha consulta a matrícula real e imprime com indicação sem validade oficial')
  for role,forbidden in [('COORDENACAO','financeiro'),('RECEPCAO','turmas')]:
   c=browser.new_context(viewport={'width':1280,'height':900});p=watch(c.new_page());pages.append(p);login(p,users[role]['email'],users[role]['password']);expect(p.locator(f'#menu a[href="#{forbidden}"]')).to_have_count(0)
   assert c.request.get(BASE+'/api/academico/'+('financeiro' if role=='COORDENACAO' else 'cursos')).status==403
   if role=='RECEPCAO':expect(p.locator('#menu a[href="/dashboard.html"]')).to_be_visible();assert c.request.get(BASE+'/api/pacientes').status==200
   ok('Menu e API aplicam permissões do perfil '+role)
  go(admin,'auditoria','Auditoria');expect(admin.get_by_text('financeiro #',exact=False).first).to_be_visible();ok('Alterações deixam trilha de auditoria real no banco')
  for key,title in [('pessoas','Pessoas e acessos'),('cursos','Cursos'),('disciplinas','Disciplinas'),('turmas','Turmas'),('matriculas','Matrículas'),('aulas','Aulas e materiais'),('atividades','Atividades'),('entregas','Entregas'),('boletim','Boletim de notas'),('financeiro','Extrato demonstrativo'),('carteirinha','Carteirinha demonstrativa'),('auditoria','Auditoria')]:
   go(admin,key,title);no_overflow(admin,'admin '+key)
  go(admin,'cursos','Cursos');expected_network=True;admin.route('**/api/academico/cursos',lambda route:route.abort());admin.reload();expect(admin.get_by_role('heading',name='Não foi possível carregar')).to_be_visible();admin.unroute('**/api/academico/cursos');admin.get_by_role('button',name='Tentar novamente').click();expect(row(admin,name)).to_be_visible();expected_network=False;ok('Falha real de rede apresenta recuperação e não simula sucesso')
  admin.goto(BASE+'/portal.html#__proto__');ready(admin);expect(admin.get_by_role('heading',name='Acesso não permitido')).to_be_visible();ok('Rota desconhecida e chave de protótipo não quebram o aplicativo')
  go(admin,'inicio','Visão geral');admin.screenshot(path=str(OUT/'admin-visao-geral.png'),full_page=True)
  small=browser.new_context(viewport={'width':320,'height':740});sp=watch(small.new_page());sp.goto(BASE+'/',wait_until='networkidle');no_overflow(sp,'login a 320px');sp.screenshot(path=str(OUT/'login-mobile.png'),full_page=True)
  nojs=browser.new_context(java_script_enabled=False,viewport={'width':390,'height':844});np=nojs.new_page();np.goto(BASE+'/');expect(np.locator('noscript')).to_be_visible();assert 'Ative o JavaScript para acessar.' in np.locator('noscript').inner_text();expect(np.locator('#email')).to_be_disabled();ok('Login sem JavaScript é explicitamente indisponível, não um formulário sem ação')
  aluno.get_by_role('button',name='Abrir navegação').click();aluno.once('dialog',lambda d:d.accept());aluno.get_by_role('button',name='Sair da conta').click();aluno.wait_for_url('**/index.html?saiu=1');assert ctxa.request.get(BASE+'/api/academico/contexto').status==401;ok('Logout revoga a sessão também na API')
  assert not errors,errors;assert not http_errors,http_errors;report(True);browser.close()
except Exception:
 traceback.print_exc()
 for p in pages:
  try:p.screenshot(path=str(OUT/'failure-'+str(pages.index(p))+'.png'),full_page=True)
  except Exception:pass
 report(False);raise
