(async () => {
  const s = await Sessao.pronta;
  document.getElementById('saudacao').textContent = 'Olá, ' + s.nome.split(' ')[0] + '!';
  document.getElementById('unidade-nome').textContent = s.unidade.nome;
  try {
    const r = await Api.get('/api/dashboard');
    document.getElementById('n-pacientes').textContent = r.pacientes;
    document.getElementById('n-especialidades').textContent = r.especialidadesAtivas;
    document.getElementById('n-atendimentos').textContent = r.atendimentos;
  } catch (e) { tratarSessao(e); }
})();
