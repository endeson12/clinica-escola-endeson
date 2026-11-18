import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { runInNewContext } from 'node:vm';

const source = readFileSync(new URL('../../src/main/resources/static/js/portal-ux.js', import.meta.url), 'utf8');

function fixture() {
  let restored = 0;
  let cancelFocused = 0;
  let init;
  const events = new Map();
  const parts = {
    h2: {}, p: {}, '[data-accept]': {}, '[data-cancel]': { focus: () => cancelFocused++ },
  };
  const dialog = {
    open: false,
    setAttribute() {},
    showModal() { this.open = true; },
    close() { this.open = false; },
    addEventListener: (event, fn) => events.set(event, fn),
    querySelector: (selector) => parts[selector],
  };
  const document = {
    activeElement: { focus: () => restored++ },
    getElementById: () => dialog,
    querySelectorAll: () => [],
    createElement: () => dialog,
    body: { append() {} },
    addEventListener(event, fn) { if (event === 'DOMContentLoaded') init = fn; },
  };
  const window = {};
  runInNewContext(source, { window, document });
  init();
  return { dialog, events, parts, confirm: window.PortalUX.confirm, focus: () => [cancelFocused, restored] };
}

test('evento de cancelamento resolve false e devolve foco ao acionador', async () => {
  const { dialog, events, confirm, focus } = fixture();
  const result = confirm('Cancelar matrícula?');
  assert.equal(dialog.open, true);
  assert.deepEqual(focus(), [1, 0]);
  let prevented = false;
  events.get('cancel')({ preventDefault: () => { prevented = true; } });
  assert.equal(await result, false);
  assert.equal(prevented, true);
  assert.equal(dialog.open, false);
  assert.deepEqual(focus(), [1, 1]);
});

test('segundo pedido não substitui confirmação em andamento', async () => {
  const { parts, confirm } = fixture();
  const result = confirm('Primeira ação');
  assert.equal(await confirm('Segunda ação'), false);
  assert.equal(parts.p.textContent, 'Primeira ação');
  parts['[data-accept]'].onclick();
  assert.equal(await result, true);
});
