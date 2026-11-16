// Browser component regression: real portal assets, explicitly mocked API.
const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../src/main/resources/static');
(async () => {
  const browser = await chromium.launch({ executablePath: process.env.CHROME_PATH || '/usr/bin/google-chrome', args: ['--no-sandbox'] });
  try {
    for (const role of ['PROFESSOR', 'ALUNO']) {
      const page = await browser.newPage();
      const requests = [], errors = [];
      page.on('pageerror', e => errors.push(e.message));
      await page.route('http://portal.test/**', async route => {
        const url = new URL(route.request().url());
        if (url.pathname.startsWith('/api/')) {
          let body = [];
          if (url.pathname.endsWith('/contexto')) body = { usuario: { id: 1, nome: 'QA LEM', perfil: role }, unidade: { id: 1, nome: 'QA isolado' } };
          if (url.pathname.endsWith('/entregas')) {
            requests.push(url.searchParams.toString());
            body = url.searchParams.get('situacao') === 'PUBLICADA' ? [] : [{ id: 1, atividade_titulo: 'Resposta QA', turma_nome: 'Turma QA', aluno_nome: 'Aluno QA', enviada_em: null, publicada: false, nota: null }];
          }
          return route.fulfill({ json: body });
        }
        const file = path.join(root, url.pathname);
        if (!fs.existsSync(file)) return route.fulfill({ status: 404, body: '' });
        return route.fulfill({ body: fs.readFileSync(file), contentType: file.endsWith('.js') ? 'application/javascript' : file.endsWith('.css') ? 'text/css' : 'text/html' });
      });
      await page.goto('http://portal.test/portal.html#entregas?atividade=77&busca=Resposta');
      const select = page.getByLabel('Situação da avaliação', { exact: true });
      await select.waitFor({ timeout: 5000 }).catch(async e => { console.error(await page.locator('body').innerText(), errors); throw e; });
      assert.equal(await select.locator('option[value="RASCUNHO"]').count(), role === 'ALUNO' ? 0 : 1);
      await select.selectOption('PUBLICADA');
      await page.waitForFunction(() => location.hash.includes('situacao=PUBLICADA') && document.querySelector('#conteudo').getAttribute('aria-busy') === 'false');
      assert(requests.some(q => q.includes('atividadeId=77') && q.includes('situacao=PUBLICADA')));
      assert(page.url().includes('busca=Resposta'));
      assert.match(await page.locator('#table-results').innerText(), /Nenhum/);
      await page.reload();
      await select.waitFor();
      assert.equal(await select.inputValue(), 'PUBLICADA');
      await select.selectOption('');
      await page.waitForFunction(() => !location.hash.includes('situacao=') && document.querySelector('#conteudo').getAttribute('aria-busy') === 'false');
      assert.match(await page.locator('#table-results').innerText(), /Resposta QA/);
      assert.deepEqual(errors, []);
      await page.close();
      console.log('PASS filtro, URL, reload, busca, atividade e estados vazios: ' + role);
    }
  } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
