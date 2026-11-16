// Destructive fixture creation is restricted to an explicitly local QA server.
const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const base = process.env.QA_BASE;
assert(base && /^http:\/\/127\.0\.0\.1:\d+$/.test(base), 'QA_BASE must be an isolated loopback server');
(async () => {
  const browser = await chromium.launch({ executablePath: process.env.CHROME_PATH || '/usr/bin/google-chrome', args: ['--no-sandbox'] });
  const password = 'SenhaTeste#123';
  const tag = 'qa-' + Date.now();
  try {
    async function actor(email) {
      const c = await browser.newContext();
      await c.request.get(base + '/api/auth/csrf');
      let token = (await c.cookies()).find(c => c.name === 'XSRF-TOKEN').value;
      let r = await c.request.post(base + '/api/auth/login', { headers: { 'X-XSRF-TOKEN': token }, data: { email, senha: password, unidadeId: 1 } });
      assert.equal(r.status(), 200);
      await c.request.get(base + '/api/auth/csrf');
      token = (await c.cookies()).find(c => c.name === 'XSRF-TOKEN').value;
      await c.setExtraHTTPHeaders({ 'X-XSRF-TOKEN': token });
      return c;
    }
    const admin = await actor('admin.teste@clinica.local');
    async function create(c, route, data) {
      const r = await c.request.post(base + '/api/academico/' + route, { data });
      assert.equal(r.status(), 201, route + ': ' + await r.text());
      return (await r.json()).id;
    }
    const people = {};
    for (const perfil of ['PROFESSOR', 'ALUNO']) {
      const email = `${tag}-${perfil}@qa.local`;
      people[perfil] = { id: await create(admin, 'pessoas', { nome: tag + perfil, email, senha: password, perfil }), context: await actor(email) };
    }
    const teacher = people.PROFESSOR.context, student = people.ALUNO.context;
    const curso = await create(admin, 'cursos', { nome: tag, codigo: tag, cargaHoraria: 120 });
    const disciplina = await create(admin, 'disciplinas', { cursoId: curso, nome: tag, codigo: tag, cargaHoraria: 60 });
    const turma = await create(admin, 'turmas', { disciplinaId: disciplina, professorId: people.PROFESSOR.id, nome: tag, periodo: '2099.1', vagas: 10 });
    await create(admin, 'matriculas', { turmaId: turma, alunoId: people.ALUNO.id });
    const atividade = await create(teacher, 'atividades', { turmaId: turma, titulo: tag, instrucoes: 'Fixture de QA', prazo: '2099-12-31T23:59:00Z' });
    const entrega = await create(student, 'entregas', { atividadeId: atividade, conteudo: 'Resposta isolada QA' });
    const page = await teacher.newPage();
    const errors = [];
    page.on('pageerror', e => errors.push(e.message));
    const url = base + '/portal.html#entregas?atividade=' + atividade;
    async function ready(p) { await p.waitForFunction(() => document.querySelector('#conteudo')?.getAttribute('aria-busy') === 'false'); }
    await page.goto(url); await ready(page);
    await page.getByLabel('Situação da avaliação', { exact: true }).selectOption('PENDENTE'); await ready(page);
    await page.locator(`[data-action="grade"][data-id="${entrega}"]`).click();
    await page.locator('#field-nota').fill('0');
    await page.locator('#field-feedback').fill('Feedback de QA');
    // Default is a private draft; publishing is an explicit checkbox.
    const check = page.locator('#entity-form input[type="checkbox"]');
    await check.uncheck();
    await page.locator('#entity-form button[type="submit"]').click();
    await page.waitForFunction(() => !document.querySelector('#entity-dialog').open); await ready(page);
    await page.locator(`[data-action="grade"][data-id="${entrega}"]`).waitFor({ state: 'hidden' });
    assert.equal(await page.locator(`[data-action="grade"][data-id="${entrega}"]`).count(), 0);
    await page.getByLabel('Situação da avaliação', { exact: true }).selectOption('RASCUNHO'); await ready(page);
    await page.locator(`[data-action="grade"][data-id="${entrega}"]`).click();
    await check.check();
    await page.locator('#entity-form button[type="submit"]').click();
    await page.waitForFunction(() => !document.querySelector('#entity-dialog').open); await ready(page);
    await page.getByLabel('Situação da avaliação', { exact: true }).selectOption('PUBLICADA'); await ready(page);
    await page.reload(); await ready(page);
    assert.equal(await page.locator(`[data-action="grade"][data-id="${entrega}"]`).count(), 1);
    const sp = await student.newPage();
    await sp.goto(url + '&situacao=PUBLICADA'); await ready(sp);
    assert.equal(await sp.locator(`[data-action="read-delivery"][data-id="${entrega}"]`).count(), 1);
    const grades = await (await student.request.get(base + '/api/academico/boletim')).json();
    assert(grades.some(g => g.id === entrega && Number(g.nota) === 0 && g.feedback === 'Feedback de QA'));
    assert.deepEqual(errors, []);
    console.log('PASS PostgreSQL + Chromium: envio → pendente → rascunho nota zero → publicação → reload → boletim aluno');
  } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
