// Comunicacao com o backend (mesma origem). Envia o token CSRF em toda requisicao que altera dados.
const Api = (() => {
  function cookie(nome) {
    const m = document.cookie.match(new RegExp('(?:^|; )' + nome + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
  }

  async function garantirCsrf() {
    if (!cookie('XSRF-TOKEN')) {
      await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    }
  }

  async function req(metodo, url, corpo) {
    const opcoes = { method: metodo, credentials: 'same-origin', headers: { 'Accept': 'application/json' } };
    if (metodo !== 'GET') {
      await garantirCsrf();
      opcoes.headers['X-XSRF-TOKEN'] = cookie('XSRF-TOKEN') || '';
    }
    if (corpo !== undefined) {
      opcoes.headers['Content-Type'] = 'application/json';
      opcoes.body = JSON.stringify(corpo);
    }
    let resp;
    try {
      resp = await fetch(url, opcoes);
    } catch (e) {
      throw { status: 0, message: 'Não foi possível conectar ao servidor. Verifique sua conexão.', fieldErrors: {} };
    }
    if (resp.status === 204) return null;
    let dados = null;
    try { dados = await resp.json(); } catch (e) { /* sem corpo */ }
    if (!resp.ok) {
      throw { status: resp.status, message: (dados && dados.message) || 'Erro inesperado (' + resp.status + ').', fieldErrors: (dados && dados.fieldErrors) || {} };
    }
    return dados;
  }

  return {
    get: (url) => req('GET', url),
    post: (url, corpo) => req('POST', url, corpo),
  };
})();

// Utilidades de formulario compartilhadas
const Form = {
  limparErros(form) {
    form.querySelectorAll('.campo').forEach(c => {
      c.classList.remove('erro');
      const m = c.querySelector('.msg-erro');
      if (m) m.textContent = '';
      const i = c.querySelector('input, select, textarea');
      if (i) i.removeAttribute('aria-invalid');
    });
  },
  erroCampo(form, nome, msg) {
    const c = form.querySelector('[data-campo="' + nome + '"]');
    if (!c) return false;
    c.classList.add('erro');
    c.querySelector('.msg-erro').textContent = msg;
    const i = c.querySelector('input, select, textarea');
    if (i) i.setAttribute('aria-invalid', 'true');
    return true;
  },
  mostrarErrosApi(form, erro, alerta) {
    let algum = false;
    Object.entries(erro.fieldErrors || {}).forEach(([k, v]) => { if (Form.erroCampo(form, k, v)) algum = true; });
    if (alerta && (!algum || erro.status >= 500 || erro.status === 0)) {
      alerta.textContent = erro.message;
      alerta.classList.add('visivel');
    }
  },
  carregando(botao, sim) {
    botao.disabled = sim;
    botao.classList.toggle('carregando', sim);
  },
  emailValido(v) { return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v); },
  cpfValido(v) {
    const c = (v || '').replace(/\D/g, '');
    if (c.length !== 11 || /^(\d)\1{10}$/.test(c)) return false;
    const dig = (n) => {
      let s = 0;
      for (let i = 0; i < n; i++) s += Number(c[i]) * (n + 1 - i);
      const r = (s * 10) % 11;
      return r === 10 ? 0 : r;
    };
    return dig(9) === Number(c[9]) && dig(10) === Number(c[10]);
  },
  mascaraCpf(v) {
    return v.replace(/\D/g, '').slice(0, 11)
      .replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  },
  mascaraTelefone(v) {
    const d = v.replace(/\D/g, '').slice(0, 11);
    if (d.length <= 10) return d.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{4})(\d)/, '$1-$2');
    return d.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2');
  },
  esc(t) {
    return String(t == null ? '' : t).replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
  },
};
