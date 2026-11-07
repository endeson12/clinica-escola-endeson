// Tela de login
(async () => {
  const form = document.getElementById('form-login');
  const alerta = document.getElementById('alerta');
  const sel = document.getElementById('unidade');
  const botao = document.getElementById('btn-entrar');
  const senha = document.getElementById('senha');
  const toggle = document.getElementById('toggle-senha');

  // ja logado? vai direto para a area interna
  try { await Api.get('/api/auth/me'); location.replace('/portal.html'); return; } catch (e) { /* segue no login */ }

  if (new URLSearchParams(location.search).get('saiu') === '1') {
    alerta.className = 'alerta alerta--ok visivel';
    alerta.textContent = 'Sessão encerrada com sucesso.';
  }

  try {
    const unidades = await Api.get('/api/unidades');
    if (!Array.isArray(unidades) || unidades.length === 0) throw new Error('Nenhuma unidade ativa está disponível.');
    sel.innerHTML = '<option value="">Selecione a unidade</option>' +
      unidades.map(u => '<option value="' + u.id + '">' + Form.esc(u.nome) + '</option>').join('');
    document.getElementById('login-fields').disabled = false;
  } catch (e) {
    sel.innerHTML = '<option value="">Não foi possível carregar</option>';
    alerta.className = 'alerta visivel';
    alerta.textContent = (e.message || 'Não foi possível carregar as unidades.') + ' Recarregue a página para tentar novamente.';
  }

  toggle.addEventListener('click', () => {
    const mostrar = senha.type === 'password';
    senha.type = mostrar ? 'text' : 'password';
    toggle.textContent = mostrar ? 'Ocultar' : 'Mostrar';
    toggle.setAttribute('aria-pressed', String(mostrar));
  });

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    Form.limparErros(form);
    alerta.className = 'alerta alerta--erro';
    const dados = { unidadeId: sel.value ? Number(sel.value) : null, email: form.email.value.trim(), senha: senha.value };
    let ok = true;
    if (!dados.unidadeId) { Form.erroCampo(form, 'unidadeId', 'Selecione a unidade.'); ok = false; }
    if (!dados.email) { Form.erroCampo(form, 'email', 'Informe o e-mail.'); ok = false; }
    else if (!Form.emailValido(dados.email)) { Form.erroCampo(form, 'email', 'Informe um e-mail válido.'); ok = false; }
    if (!dados.senha) { Form.erroCampo(form, 'senha', 'Informe a senha.'); ok = false; }
    if (!ok) return;

    Form.carregando(botao, true);
    botao.querySelector('.rotulo').textContent = 'Entrando...';
    try {
      await Api.post('/api/auth/login', dados);
      location.replace('/portal.html');
    } catch (e) {
      if (e.status === 403 && e.fieldErrors && e.fieldErrors.unidadeId) {
        Form.erroCampo(form, 'unidadeId', e.fieldErrors.unidadeId);
      } else if (e.status === 400) {
        Form.mostrarErrosApi(form, e, alerta);
      } else {
        alerta.textContent = e.message;
        alerta.classList.add('visivel');
      }
      Form.carregando(botao, false);
      botao.querySelector('.rotulo').textContent = 'Entrar';
    }
  });
})();
