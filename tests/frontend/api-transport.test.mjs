import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { runInNewContext } from 'node:vm';

const source = readFileSync(new URL('../../src/main/resources/static/js/api.js', import.meta.url), 'utf8');
const load = (fetch, document = { cookie: '' }) => runInNewContext(`${source}
Api`, { fetch, document });

test('PATCH inicializa e envia CSRF com JSON de mesma origem', async () => {
  const calls = [];
  const document = { cookie: '' };
  const api = load(async (url, options) => {
    calls.push({ url, options });
    if (calls.length === 1) document.cookie = 'XSRF-TOKEN=fixture%2Bvalor';
    return { status: 204, json() { assert.fail('204 não tem corpo'); } };
  }, document);
  assert.equal(await api.patch('/api/exemplo', { ativo: false }), null);
  assert.deepEqual(calls.map(({ url }) => url), ['/api/auth/csrf', '/api/exemplo']);
  assert.equal(calls[0].options.credentials, 'same-origin');
  const { options } = calls[1];
  assert.equal(options.method, 'PATCH');
  assert.equal(options.credentials, 'same-origin');
  assert.equal(options.headers['X-XSRF-TOKEN'], 'fixture+valor');
  assert.equal(options.headers['Content-Type'], 'application/json');
  assert.deepEqual(JSON.parse(options.body), { ativo: false });
});

test('GET preserva erro com campos e não inicializa CSRF', async () => {
  const calls = [];
  const api = load(async (url, options) => {
    calls.push(url);
    assert.equal(options.method, 'GET');
    assert.equal(options.headers['X-XSRF-TOKEN'], undefined);
    return { status: 400, ok: false, json: async () => ({ message: 'Verifique a entrada', fieldErrors: { nome: 'Obrigatório' } }) };
  });
  await assert.rejects(api.get('/api/exemplo'), (error) => {
    assert.equal(error.status, 400);
    assert.equal(error.message, 'Verifique a entrada');
    assert.equal(error.fieldErrors.nome, 'Obrigatório');
    return true;
  });
  assert.deepEqual(calls, ['/api/exemplo']);
});
