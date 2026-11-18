import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { runInNewContext } from 'node:vm';

const source = readFileSync(new URL('../../src/main/resources/static/js/theme.js', import.meta.url), 'utf8');

function setup({ saved = null, systemDark = false, blocked = false } = {}) {
  const callbacks = new Map();
  const attributes = new Map();
  const root = { dataset: {}, style: {} };
  const writes = [];
  const button = {
    setAttribute: (key, value) => attributes.set(key, value),
    addEventListener: (event, fn) => callbacks.set(event, fn),
  };
  const document = {
    documentElement: root,
    querySelector: () => button,
    querySelectorAll: () => [button],
    addEventListener: (event, fn) => callbacks.set(event, fn),
  };
  const media = { matches: systemDark, addEventListener: (event, fn) => callbacks.set(event, fn) };
  const localStorage = {
    getItem() { if (blocked) throw new Error('Storage indisponível'); return saved; },
    setItem(key, value) { if (blocked) throw new Error('Storage indisponível'); writes.push([key, value]); },
  };
  runInNewContext(source, { document, localStorage, matchMedia: () => media });
  callbacks.get('DOMContentLoaded')();
  return { callbacks, attributes, root, writes };
}

test('sem preferência segue o sistema até a primeira escolha explícita', () => {
  const { callbacks, root, writes, attributes } = setup();
  assert.equal(root.dataset.theme, 'light');
  callbacks.get('change')({ matches: true });
  assert.equal(root.style.colorScheme, 'dark');
  assert.equal(attributes.get('aria-label'), 'Ativar modo claro');
  callbacks.get('click')();
  callbacks.get('change')({ matches: true });
  assert.equal(root.dataset.theme, 'light');
  assert.equal(attributes.get('aria-label'), 'Ativar modo escuro');
  assert.deepEqual(writes, [['portal-color-theme', 'light']]);
});

test('armazenamento indisponível não impede alternância durante a página', () => {
  const { callbacks, root } = setup({ systemDark: true, blocked: true });
  assert.equal(root.dataset.theme, 'dark');
  callbacks.get('click')();
  assert.equal(root.style.colorScheme, 'light');
});
