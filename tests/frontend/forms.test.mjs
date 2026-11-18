import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { runInNewContext } from 'node:vm';

const source = readFileSync(new URL('../../src/main/resources/static/js/api.js', import.meta.url), 'utf8');
const Form = runInNewContext(`${source}
Form`);

function fixture() {
  const attributes = new Map();
  const classes = new Set();
  const message = { textContent: '' };
  const input = {
    setAttribute: (name, value) => attributes.set(name, value),
    removeAttribute: (name) => attributes.delete(name),
  };
  const field = {
    classList: { add: (name) => classes.add(name), remove: (name) => classes.delete(name) },
    querySelector: (selector) => ({ '.msg-erro': message, 'input, select, textarea': input })[selector],
  };
  const form = {
    querySelector: (selector) => selector === '[data-campo="email"]' ? field : null,
    querySelectorAll: (selector) => selector === '.campo' ? [field] : [],
  };
  return { form, attributes, classes, message };
}

test('erro de campo adiciona aria-invalid e a limpeza remove o estado', () => {
  const { form, attributes, classes, message } = fixture();
  assert.equal(Form.erroCampo(form, 'email', 'E-mail inválido'), true);
  assert.equal(attributes.get('aria-invalid'), 'true');
  assert.equal(classes.has('erro'), true);
  assert.equal(message.textContent, 'E-mail inválido');
  Form.limparErros(form);
  assert.equal(attributes.has('aria-invalid'), false);
  assert.equal(classes.has('erro'), false);
  assert.equal(message.textContent, '');
});

test('campo não renderizado usa o alerta geral', () => {
  const { form } = fixture();
  const classes = new Set();
  const alert = { classList: { add: (name) => classes.add(name) } };
  Form.mostrarErrosApi(form, { status: 400, message: 'Revise os dados', fieldErrors: { ausente: 'Obrigatório' } }, alert);
  assert.equal(alert.textContent, 'Revise os dados');
  assert.equal(classes.has('visivel'), true);
});
