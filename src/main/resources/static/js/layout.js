// Protege as paginas internas e preenche o cabecalho (usuario, unidade, sair)
const Sessao = (() => {
  let atual = null;
  const pronta = (async () => {
    try {
      atual = await Api.get('/api/auth/me');
    } catch (e) {
      location.replace('/index.html');
      throw e;
    }
    document.getElementById('chip-unidade').textContent = atual.unidade.nome;
    document.getElementById('nome-usuario').textContent = atual.nome;
    const nav = document.querySelector('nav');
    if (nav) {
      const link = document.createElement('a');
      link.href = '/portal.html';
      link.textContent = 'Portal institucional ↗';
      nav.prepend(link);
    }
    return atual;
  })();

  document.getElementById('btn-sair').addEventListener('click', async (ev) => {
    ev.target.disabled = true;
    try { await Api.post('/api/auth/logout'); } catch (e) { /* mesmo assim volta ao login */ }
    location.replace('/index.html?saiu=1');
  });

  return { pronta };
})();

function toast(msg, tipo) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast toast--' + (tipo || 'ok') + ' visivel';
  clearTimeout(t._timer);
  t._timer = setTimeout(() => t.classList.remove('visivel'), 3500);
}

function dataBR(iso) {
  if (!iso) return '–';
  const [d] = iso.split('T');
  const [a, m, dia] = d.split('-');
  return dia + '/' + m + '/' + a;
}

function dataHoraBR(iso) {
  if (!iso) return '–';
  const [d, h] = iso.split('T');
  return dataBR(d) + ' ' + (h || '').slice(0, 5);
}

// em caso de sessao expirada durante o uso
function tratarSessao(e) {
  if (e && e.status === 401) { location.replace('/index.html'); return true; }
  return false;
}
