(async () => {
  await Sessao.pronta;
  const form = document.getElementById('form');
  const alerta = document.getElementById('alerta');
  const lista = document.getElementById('lista');
  const botao = form.querySelector('button[type=submit]');
  const rotuloStatus = { AGENDADO: 'Agendado', REALIZADO: 'Realizado', CANCELADO: 'Cancelado' };

  async function carregarOpcoes() {
    const [pacientes, especialidades] = await Promise.all([
      Api.get('/api/pacientes'), Api.get('/api/especialidades?ativas=true')]);
    form.pacienteId.innerHTML = pacientes.length
      ? '<option value="">Selecione o paciente</option>' + pacientes.map(p => '<option value="' + p.id + '">' + Form.esc(p.nomeCompleto) + ' (' + Form.esc(p.cpf) + ')</option>').join('')
      : '<option value="">Cadastre um paciente primeiro</option>';
    form.especialidadeId.innerHTML = especialidades.length
      ? '<option value="">Selecione a especialidade</option>' + especialidades.map(e => '<option value="' + e.id + '">' + Form.esc(e.nome) + '</option>').join('')
      : '<option value="">Cadastre uma especialidade ativa</option>';
  }

  async function carregar() {
    try {
      const itens = await Api.get('/api/atendimentos');
      document.getElementById('contador').textContent = itens.length + (itens.length === 1 ? ' atendimento' : ' atendimentos');
      lista.innerHTML = itens.length ? itens.map(a => '<tr>' +
        '<td data-label="Data e hora"><strong>' + dataHoraBR(a.dataHora) + '</strong></td>' +
        '<td data-label="Paciente">' + Form.esc(a.paciente) + '</td>' +
        '<td data-label="Especialidade">' + Form.esc(a.especialidade) + '</td>' +
        '<td data-label="Status"><span class="badge badge--' + a.status + '">' + rotuloStatus[a.status] + '</span></td>' +
        '<td data-label="Unidade">' + Form.esc(a.unidade) + '</td></tr>').join('')
        : '<tr><td colspan="5" class="vazio">Nenhum atendimento registrado nesta unidade.</td></tr>';
    } catch (e) {
      if (!tratarSessao(e)) lista.innerHTML = '<tr><td colspan="5" class="vazio">Erro ao carregar: ' + Form.esc(e.message) + '</td></tr>';
    }
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    Form.limparErros(form);
    alerta.classList.remove('visivel');
    const dados = {
      pacienteId: form.pacienteId.value ? Number(form.pacienteId.value) : null,
      especialidadeId: form.especialidadeId.value ? Number(form.especialidadeId.value) : null,
      dataHora: form.dataHora.value || null,
      status: form.status.value,
      observacoes: form.observacoes.value.trim() || null,
    };
    let ok = true;
    if (!dados.pacienteId) { Form.erroCampo(form, 'pacienteId', 'Selecione o paciente.'); ok = false; }
    if (!dados.especialidadeId) { Form.erroCampo(form, 'especialidadeId', 'Selecione a especialidade.'); ok = false; }
    if (!dados.dataHora) { Form.erroCampo(form, 'dataHora', 'Informe a data e o horário.'); ok = false; }
    if (!ok) return;
    Form.carregando(botao, true);
    try {
      await Api.post('/api/atendimentos', dados);
      form.reset();
      toast('Atendimento registrado com sucesso.');
      await carregar();
    } catch (e) {
      if (!tratarSessao(e)) { Form.mostrarErrosApi(form, e, alerta); toast('Não foi possível salvar.', 'erro'); }
    } finally {
      Form.carregando(botao, false);
    }
  });

  try { await carregarOpcoes(); } catch (e) { tratarSessao(e); }
  carregar();
})();
