"""Verificação real de marca e artes enviadas; credenciais somente em arquivo privado."""
import argparse
import hashlib
import json
from pathlib import Path
from playwright.sync_api import sync_playwright, expect

ap = argparse.ArgumentParser()
ap.add_argument('--base', required=True)
ap.add_argument('--access-json', required=True)
ap.add_argument('--output', required=True)
a = ap.parse_args()
base = a.base.rstrip('/')
source = Path(a.access_json)
assert source.stat().st_mode & 0o777 == 0o600
actor = json.loads(source.read_text())
out = Path(a.output)
out.mkdir(parents=True, exist_ok=True)
static = Path(__file__).resolve().parents[1] / 'src/main/resources/static'
checks, errors = [], []
def ok(name):
    checks.append(name)
    print('OK', name, flush=True)
def settle(p):
    p.evaluate('''async () => {await document.fonts.ready;
      await Promise.all([...document.images].filter(i => i.getBoundingClientRect().width > 0).map(i => {i.loading='eager'; return i.decode()}));}''')
def overflow(p):
    assert p.evaluate('document.documentElement.scrollWidth <= innerWidth + 1')
def ready(p):
    expect(p.locator('#conteudo')).to_have_attribute('aria-busy', 'false')
def logo(p):
    expect(p.locator('.brand-mark')).to_be_visible()
    assert p.locator('.brand-mark').evaluate('(i)=>i.complete && i.naturalWidth===119 && i.naturalHeight===155')

with sync_playwright() as pw:
    b = pw.chromium.launch(executable_path='/usr/bin/google-chrome', headless=True, args=['--no-sandbox'])
    c = b.new_context(viewport={'width':1440,'height':1000})
    p = c.new_page()
    p.on('pageerror', lambda e: errors.append(str(e)))
    p.goto(base+'/', wait_until='networkidle')
    settle(p); logo(p); overflow(p)
    expect(p.locator('link[rel=icon]')).to_have_attribute('href','/favicon.ico')
    p.screenshot(path=str(out/'login-desktop.png'), full_page=True, animations='disabled')
    ok('Login HTTPS com brasão fornecido e favicon correto')
    resources = ['/favicon.ico','/img/uninassau-brasao.png','/img/uninassau-icon.png','/css/brand.css'] + [f'/img/arte-enviada-{i:02}.webp' for i in range(1,9)]
    for resource in resources:
        r = c.request.get(base+resource)
        assert r.status == 200, resource
        assert r.body() == (static/resource.lstrip('/')).read_bytes(), resource
        ok('Recurso HTTPS idêntico ao arquivo local: '+resource)
    p.locator('#unidade').select_option('1')
    p.locator('#email').fill(actor['email'])
    p.locator('#senha').fill(actor['password'])
    p.get_by_role('button',name='Entrar no portal',exact=True).click()
    p.wait_for_url('**/portal.html'); ready(p); settle(p); logo(p)
    assert c.request.get(base+'/portal.html').body()==(static/'portal.html').read_bytes()
    ok('Login real preservado e portal público atualizado byte a byte')
    expect(p.locator('[data-gallery-slide]')).to_have_count(11)
    for i in range(1,9):
        p.locator(f'[data-gallery-slide="{i+2}"]').click()
        expect(p.locator('#gallery-stage')).to_have_class('gallery-stage user-art')
        im = p.locator('#gallery-stage > img').first
        expect(im).to_have_attribute('src', f'/img/arte-enviada-{i:02}.webp')
        im.evaluate('(i)=>i.decode()')
        assert im.evaluate('(i)=>i.naturalWidth>0 && getComputedStyle(i).objectFit==="contain"')
        expect(p.locator('#gallery-caption')).to_contain_text('Arte enviada pelo usuário')
        expect(p.locator(f'[data-gallery-slide="{i+2}"]')).to_have_attribute('aria-pressed','true')
        ok(f'Arte enviada {i:02}: imagem real, legenda, proporção e controle sincronizados')
    p.locator('[data-gallery-slide="9"]').click(); settle(p)
    p.evaluate('scrollTo(0,0)')
    p.screenshot(path=str(out/'portal-desktop.png'), full_page=True, animations='disabled')
    p.locator('.gallery').screenshot(path=str(out/'galeria-brasao.png'), animations='disabled')
    for width in [320,390,768,1024,1440]:
        p.set_viewport_size({'width':width,'height':900});overflow(p)
        if width <= 780:
            expect(p.locator('.mobile-brand')).to_be_visible()
        if width==390:
            p.evaluate('scrollTo(0,0)')
            p.screenshot(path=str(out/'portal-mobile.png'),full_page=True,animations='disabled')
        ok(f'Portal e 11 controles da galeria sem overflow a {width}px')
    p.emulate_media(media='print')
    expect(p.locator('.print-brand')).to_be_visible()
    expect(p.locator('.print-brand')).to_contain_text('sem validade oficial')
    assert p.locator('.print-brand img').evaluate('(i)=>i.complete && i.naturalWidth>0')
    p.pdf(path=str(out/'impressao-demonstrativa.pdf'),format='A4')
    ok('Cabeçalho de impressão com brasão e aviso demonstrativo')
    p.emulate_media(media='screen');p.set_viewport_size({'width':1440,'height':1000})
    for name in ['dashboard.html','pacientes.html','especialidades.html','atendimentos.html']:
        p.goto(base+'/'+name,wait_until='networkidle');settle(p);logo(p)
        expect(p.get_by_role('link',name='Portal institucional ↗')).to_be_visible()
        expect(p.locator('link[rel=icon]')).to_have_attribute('href','/favicon.ico')
        assert c.request.get(base+'/'+name).body()==(static/name).read_bytes()
        for width in [390,1440]:
            p.set_viewport_size({'width':width,'height':900});overflow(p)
        ok('Marca e navegação clínica preservadas: '+name)
    p.goto(base+'/portal.html#carteirinha',wait_until='networkidle');ready(p)
    select=p.locator('#card-student')
    values=select.locator('option').evaluate_all('(xs)=>xs.map(x=>x.value).filter(Boolean)')
    if values:
        select.select_option(values[0]);ready(p)
        if p.locator('.identity-brand').count():
            expect(p.locator('.identity-brand')).to_be_visible()
            expect(p.locator('.identity-warning')).to_contain_text('SEM VALIDADE OFICIAL')
            ok('Carteirinha usa brasão sem remover aviso de ausência de validade oficial')
        else:
            ok('Carteirinha indisponível sem matrícula ativa; ausência de dados respeitada')
    p.once('dialog', lambda dialog: dialog.accept())
    p.get_by_role('button',name='Sair da conta',exact=True).click()
    p.wait_for_url('**/index.html*')
    p.set_viewport_size({'width':390,'height':844});settle(p);logo(p);overflow(p)
    p.screenshot(path=str(out/'login-mobile.png'),full_page=True,animations='disabled')
    ok('Logout e login mobile com brasão e sem overflow')
    assert not errors,errors
    b.close()
receipt={'success':True,'checks':checks,'page_errors':errors,'base':base,'resources':resources}
serialized=json.dumps(receipt,ensure_ascii=False,indent=2)
(out/'brand-results.json').write_text(serialized)
assert json.loads((out/'brand-results.json').read_text())==receipt
print(json.dumps({'success':True,'checks':len(checks),'page_errors':len(errors),'evidence':str(out/'brand-results.json')},ensure_ascii=False))
