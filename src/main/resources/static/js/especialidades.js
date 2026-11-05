(async () => {
  await Sessao.pronta;
  const form = document.getElementById('form');
  const alerta = document.getElementById('alerta');
  const lista = document.getElementById('lista');
  const botao = form.querySelector('button[type=submit]');

  async function carregar() {
    try {
      const itens = await Api.get('/api/especialidades');
      document.getElementById('contador').textContent = itens.length + (itens.length === 1 ? ' especialidade' : ' especialidades');
      lista.innerHTML = itens.length ? itens.map(e => '<tr>' +
        '<td data-label="Nome"><strong>' + Form.esc(e.nome) + '</strong></td>' +
        '<td data-label="Descrição">' + Form.esc(e.descricao || '–') + '</td>' +
        '<td data-label="Situação"><span class="badge badge--' + (e.ativa ? 'ativa">Ativa' : 'inativa">Inativa') + '</span></td></tr>').join('')
        : '<tr><td colspan="3" class="vazio">Nenhuma especialidade cadastrada.</td></tr>';
    } catch (e) {
      if (!tratarSessao(e)) lista.innerHTML = '<tr><td colspan="3" class="vazio">Erro ao carregar: ' + Form.esc(e.message) + '</td></tr>';
    }
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    Form.limparErros(form);
    alerta.classList.remove('visivel');
    const dados = { nome: form.nome.value.trim(), descricao: form.descricao.value.trim() || null, ativa: form.ativa.value === 'true' };
    if (!dados.nome) { Form.erroCampo(form, 'nome', 'Informe o nome da especialidade.'); return; }
    Form.carregando(botao, true);
    try {
      const e = await Api.post('/api/especialidades', dados);
      form.reset();
      toast('Especialidade ' + e.nome + ' cadastrada.');
      await carregar();
    } catch (e) {
      if (!tratarSessao(e)) { Form.mostrarErrosApi(form, e, alerta); toast('Não foi possível salvar.', 'erro'); }
    } finally {
      Form.carregando(botao, false);
    }
  });

  carregar();
})();
