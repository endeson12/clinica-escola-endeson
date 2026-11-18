import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { runInNewContext } from 'node:vm';

import { setImmediate } from 'node:timers/promises';

const source = readFileSync(new URL('../../src/main/resources/static/js/auth.js', import.meta.url), 'utf8');

async function setup({ logged = false, units = [] } = {}) {
  const calls = [];
  const ids = Object.fromEntries(['form-login', 'alerta', 'unidade', 'btn-entrar', 'senha', 'toggle-senha', 'login-fields']
    .map((id) => [id, { disabled: true, addEventListener() {} }]));
  const redirects = [];
  const Api = {
    async get(url) {
      calls.push(url);
      if (url === '/api/auth/me') {
        if (logged) return {};
        throw { status: 401 };
      }
      if (url === '/api/unidades') return units;
      assert.fail(`Rota inesperada: ${url}`);
    },
  };
  runInNewContext(source, {
    document: { getElementById: (id) => ids[id] }, Api, URLSearchParams,
    location: { search: '', replace: (url) => redirects.push(url) },
  });
  await setImmediate();
  return { calls, ids, redirects };
}

test('sem unidades o formulário fica inativo com instrução para recuperar', async () => {
  const { calls, ids, redirects } = await setup();
  assert.deepEqual(calls, ['/api/auth/me', '/api/unidades']);
  assert.deepEqual(redirects, []);
  assert.equal(ids['login-fields'].disabled, true);
  assert.match(ids.unidade.innerHTML, /Não foi possível carregar/);
  assert.match(ids.alerta.textContent, /Recarregue a página/);
});

test('sessão existente redireciona sem carregar a lista de unidades', async () => {
  const { calls, redirects } = await setup({ logged: true });
  assert.deepEqual(calls, ['/api/auth/me']);
  assert.deepEqual(redirects, ['/portal.html']);
});
