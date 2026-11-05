(async () => {
  await Sessao.pronta;
  const form = document.getElementById('form');
  const alerta = document.getElementById('alerta');
  const lista = document.getElementById('lista');
  const botao = form.querySelector('button[type=submit]');

  form.cpf.addEventListener('input', () => { form.cpf.value = Form.mascaraCpf(form.cpf.value); });
  form.telefone.addEventListener('input', () => { form.telefone.value = Form.mascaraTelefone(form.telefone.value); });
  form.dataNascimento.max = new Date().toISOString().slice(0, 10);

  async function carregar() {
    try {
      const itens = await Api.get('/api/pacientes');
      document.getElementById('contador').textContent = itens.length + (itens.length === 1 ? ' paciente' : ' pacientes');
      lista.innerHTML = itens.length ? itens.map(p => '<tr>' +
        '<td data-label="Nome"><strong>' + Form.esc(p.nomeCompleto) + '</strong></td>' +
        '<td data-label="CPF">' + Form.esc(p.cpf) + '</td>' +
        '<td data-label="E-mail">' + Form.esc(p.email) + '</td>' +
        '<td data-label="Telefone">' + Form.esc(p.telefone ? Form.mascaraTelefone(p.telefone) : '–') + '</td>' +
        '<td data-label="Nascimento">' + dataBR(p.dataNascimento) + '</td></tr>').join('')
        : '<tr><td colspan="5" class="vazio">Nenhum paciente cadastrado nesta unidade.</td></tr>';
    } catch (e) {
      if (!tratarSessao(e)) lista.innerHTML = '<tr><td colspan="5" class="vazio">Erro ao carregar: ' + Form.esc(e.message) + '</td></tr>';
    }
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    Form.limparErros(form);
    alerta.classList.remove('visivel');
    const dados = {
      nomeCompleto: form.nomeCompleto.value.trim(),
      cpf: form.cpf.value.trim(),
      email: form.email.value.trim(),
      telefone: form.telefone.value.trim() || null,
      dataNascimento: form.dataNascimento.value || null,
    };
    let ok = true;
    if (!dados.nomeCompleto) { Form.erroCampo(form, 'nomeCompleto', 'Informe o nome completo.'); ok = false; }
    if (!dados.cpf) { Form.erroCampo(form, 'cpf', 'Informe o CPF.'); ok = false; }
    else if (!Form.cpfValido(dados.cpf)) { Form.erroCampo(form, 'cpf', 'CPF inválido.'); ok = false; }
    if (!dados.email) { Form.erroCampo(form, 'email', 'Informe o e-mail.'); ok = false; }
    else if (!Form.emailValido(dados.email)) { Form.erroCampo(form, 'email', 'Informe um e-mail válido.'); ok = false; }
    if (!ok) return;

    Form.carregando(botao, true);
    try {
      const p = await Api.post('/api/pacientes', dados);
      form.reset();
      toast('Paciente ' + p.nomeCompleto + ' cadastrado com sucesso.');
      await carregar();
    } catch (e) {
      if (!tratarSessao(e)) { Form.mostrarErrosApi(form, e, alerta); toast('Não foi possível salvar.', 'erro'); }
    } finally {
      Form.carregando(botao, false);
    }
  });

  carregar();
})();
