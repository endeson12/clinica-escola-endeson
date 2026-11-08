/* Academic self-service. All records come from the authenticated Java API. */
window.PortalServices = (() => {
  "use strict";
  const $ = (q, r = document) => r.querySelector(q),
    esc = Form.esc,
    I = (n) => PortalUX.icon(n);
  const all = [
      "ADMIN",
      "COORDENACAO",
      "PROFESSOR",
      "ALUNO",
      "FINANCEIRO",
      "RECEPCAO",
    ],
    academic = all.slice(0, 4);
  const routes = {
    "meu-curso": {
      name: "Meu curso",
      desc: "Disciplinas, materiais e avaliações por período letivo.",
      group: "VIDA ACADÊMICA",
      roles: academic,
      icon: "book",
    },
    horarios: {
      name: "Quadro de horários",
      desc: "Grade semanal das turmas, com professor, sala e horário.",
      group: "VIDA ACADÊMICA",
      roles: academic,
      icon: "clock",
    },
    calendario: {
      name: "Calendário",
      desc: "Prazos publicados das atividades das suas turmas.",
      group: "VIDA ACADÊMICA",
      roles: academic,
      icon: "calendar",
    },
    complementares: {
      name: "Atividades complementares",
      desc: "Envio, análise e acompanhamento de horas complementares.",
      group: "VIDA ACADÊMICA",
      roles: ["ADMIN", "COORDENACAO", "ALUNO"],
      icon: "award",
    },
    solicitacoes: {
      name: "Solicitações",
      desc: "Abra protocolos internos e acompanhe respostas da gestão.",
      group: "ATENDIMENTO",
      roles: all,
      icon: "message",
    },
    recursos: {
      name: "Biblioteca e links",
      desc: "Materiais publicados e orientação sobre serviços externos.",
      group: "ATENDIMENTO",
      roles: all,
      icon: "layers",
    },
    perfil: {
      name: "Meu perfil",
      desc: "Contato, endereço e escolaridade na sua unidade.",
      group: "MINHA CONTA",
      roles: all,
      icon: "user",
    },
  };
  const types = {
    DOCUMENTOS: "Declarações e históricos",
    SEGUNDA_CHAMADA: "Segunda chamada",
    DISPENSA: "Dispensa de disciplinas",
    MUDANCA_CURSO: "Mudança de curso",
    TRANSFERENCIA: "Transferência",
    MUDANCA_TURMA: "Mudança de turma",
    MUDANCA_TURNO: "Mudança de turno",
    INCLUSAO_DISCIPLINA: "Incluir disciplina",
    CERTIFICADOS: "Certificados e diplomas",
    ATENDIMENTO: "Atendimento",
    OUTROS: "Outros assuntos",
  };
  const labels = {
    ABERTA: "Aberta",
    RESPONDIDA: "Respondida",
    CANCELADA: "Cancelada",
    PENDENTE: "Pendente",
    APROVADA: "Aprovada",
    REPROVADA: "Reprovada",
  };
  let H,
    cache = {},
    modalFocus,
    month = new Date().getMonth(),
    year = new Date().getFullYear(),
    step = 0,
    tourFocus;
  const get = (path) => Api.get("/api/academico/" + path),
    post = (path, v = {}) => Api.post("/api/academico/" + path, v),
    put = (path, v) => Api.put("/api/academico/" + path, v);
  const context = () => H.ctx(),
    manager = () => ["ADMIN", "COORDENACAO"].includes(context().usuario.perfil),
    isAcademic = () => academic.includes(context().usuario.perfil);
  const date = (v, time = false) =>
    v
      ? new Intl.DateTimeFormat(
          "pt-BR",
          time
            ? { dateStyle: "short", timeStyle: "short" }
            : { dateStyle: "short" },
        ).format(new Date(time ? v : String(v).slice(0, 10) + "T12:00:00"))
      : "—";
  const badge = (s) =>
    `<span class="badge ${["RESPONDIDA", "APROVADA"].includes(s) ? "green" : ["ABERTA", "PENDENTE"].includes(s) ? "amber" : ""}">${esc(labels[s] || s)}</span>`;
  const button = (action, label, id = "", cls = "") =>
    `<button class="btn ${cls}" data-service="${action}" ${id !== "" ? `data-service-id="${Number(id)}"` : ""}>${esc(label)}</button>`;
  const title = (text, description, actions = "") =>
    H.heading(text, description, actions, "SEU PORTAL");
  const empty = (name, description) =>
    `<div class="service-empty">${I("grid")}<h2>${esc(name)}</h2><p>${esc(description)}</p></div>`;
  const options = (obj) =>
    Object.entries(obj).map(([value, label]) => ({ value, label }));
  const quick = () =>
    `<div class="service-quick-links"><a class="btn" href="#horarios">${I("clock")}Horários</a><a class="btn" href="#calendario">${I("calendar")}Calendário</a><a class="btn" href="#solicitacoes">${I("message")}Solicitações</a></div>`;
  function close() {
    const d = $("#service-dialog");
    d.close();
    modalFocus?.focus();
  }
  function show(name, html) {
    modalFocus = document.activeElement;
    $("#service-title").textContent = name;
    $("#service-body").innerHTML = html;
    $("#service-dialog").showModal();
  }
  const safe = (v) => {
    try {
      const u = new URL(v);
      return u.protocol === "https:" && !u.username && !u.password
        ? u.href
        : null;
    } catch {
      return null;
    }
  };
  function links(items) {
    return items
      .map(
        (r) =>
          `<article><h3>${esc(r.titulo)}</h3><p>${esc(r.conteudo || "")}</p>${safe(r.material_url) ? `<footer><a href="${esc(safe(r.material_url))}" target="_blank" rel="noopener noreferrer">Abrir material HTTPS ↗</a></footer>` : ""}</article>`,
      )
      .join("");
  }
  function renderCourse(data) {
    const [turmas, aulas, grades] = data;
    cache.course = { turmas, aulas, grades };
    const periods = [...new Set(turmas.map((t) => t.periodo))].sort((a, b) => {
      // Academic semesters precede arbitrary legacy/demo labels.
      const semester = (value) => /^\d{4}\.[12]$/.test(value);
      return Number(semester(b)) - Number(semester(a)) || b.localeCompare(a);
    });
    let chosen =
      new URLSearchParams(location.hash.split("?")[1]).get("periodo") ||
      periods[0] ||
      "";
    if (!periods.includes(chosen)) chosen = periods[0] || "";
    return (
      title(
        "Meu curso",
        "Disciplinas, professores e materiais dos períodos cadastrados no portal.",
      ) +
      `<div class="welcome-strip"><div><h2>Seu semestre, com mais clareza.</h2><p>Consulte materiais, avaliações e serviços sem perder o contexto.</p></div>${button("tour", "Conhecer o portal")}</div><div class="service-toolbar"><label class="period-selector">Período letivo<select id="course-period" ${periods.length ? "" : "disabled"}>${periods.map((v) => `<option ${v === chosen ? "selected" : ""}>${esc(v)}</option>`).join("") || "<option>Sem períodos cadastrados</option>"}</select></label>${quick()}</div>` +
      (turmas.length
        ? `<div class="course-grid">${turmas
            .filter((t) => t.periodo === chosen)
            .map((t) => {
              const material = aulas.filter(
                  (a) => Number(a.turma_id) === Number(t.id),
                ).length,
                notes = grades.filter(
                  (g) => Number(g.turma_id) === Number(t.id),
                );
              return `<article class="course-card"><header><div class="course-meta"><span>${esc(t.curso_nome || t.nome)}</span><span>${esc(t.periodo)}</span></div><h2>${esc(t.disciplina_nome)}</h2></header><div class="course-body"><p>${esc(t.nome)}</p><p>Professor(a) <strong>${esc(t.professor_nome)}</strong></p><div class="course-stat">${I("award")}<span>${notes.length ? `${notes.length} avaliação(ões) publicada(s)` : "Nenhuma avaliação publicada"}</span></div><p>Frequência e aprovação institucional não são calculadas neste portal.</p></div><footer class="course-actions">${button("materials", "Materiais (" + material + ")", t.id)}${button("grades", "Ver avaliações", t.id)}<a class="btn small" href="#atividades?turma=${Number(t.id)}">Atividades</a></footer></article>`;
            })
            .join("")}</div>`
        : empty(
            "Nenhuma turma vinculada.",
            "As disciplinas aparecem após o cadastro da turma e a matrícula ou vínculo docente.",
          ))
    );
  }
  function profile(data) {
    cache.profile = data;
    const list = (entries) =>
      `<dl class="data-list">${entries.map(([label, value]) => `<div><dt>${esc(label)}</dt><dd>${esc(value || "Não informado")}</dd></div>`).join("")}</dl>`;
    return (
      title(
        "Meu perfil",
        "Seus dados de contato, endereço e escolaridade nesta unidade.",
        button("edit-profile", "Editar meus dados", "", "primary"),
      ) +
      `<div class="profile-grid"><section class="panel profile-section"><h2>Identificação</h2>${list(
        [
          ["Nome", data.nome],
          ["E-mail de acesso", data.email],
          ["Perfil", data.perfil],
          ["Unidade", data.unidade],
        ],
      )}<p class="dialog-help">A identificação e as permissões são gerenciadas pela administração. Solicite uma correção em Solicitações.</p></section><section class="panel profile-section"><h2>Contato e endereço</h2>${list(
        [
          ["Telefone", data.telefone],
          ["CEP", data.cep],
          ["Endereço", [data.endereco, data.numero].filter(Boolean).join(", ")],
          ["Complemento", data.complemento],
          ["Bairro", data.bairro],
          ["Cidade/UF", [data.cidade, data.estado].filter(Boolean).join(" / ")],
        ],
      )}</section><section class="panel profile-section"><h2>Escolaridade anterior</h2>${list(
        [
          ["Escola", data.escola],
          ["Ano de conclusão", data.ano_conclusao],
        ],
      )}</section><section class="panel profile-section"><h2>Aparência e acesso</h2><p>O botão de lua/sol na barra superior alterna o modo claro e escuro. A preferência fica salva neste navegador.</p><p class="dialog-help">Este portal independente não utiliza a senha ou a sessão dos sistemas institucionais externos.</p><a class="btn" href="#solicitacoes">Solicitar suporte</a></section></div>`
    );
  }
  function requests(data) {
    cache.requests = data;
    return (
      title(
        "Solicitações",
        "Protocolos internos de homologação. Nenhum documento institucional é emitido automaticamente.",
        button("new-request", "Nova solicitação", "", "primary"),
      ) +
      `<div class="service-toolbar"><label class="service-filter">Situação<select id="request-status"><option value="">Todas</option><option value="ABERTA">Abertas</option><option value="RESPONDIDA">Respondidas</option><option value="CANCELADA">Canceladas</option></select></label><input type="search" class="service-search" id="request-search" aria-label="Buscar solicitação" placeholder="Buscar por protocolo ou assunto"></div><div class="request-list" id="requests-list">${requestCards(data)}</div>`
    );
  }
  function requestCards(data) {
    return (
      data
        .map(
          (r) =>
            `<article class="request-card"><div class="course-meta"><span>PROTOCOLO #${Number(r.id)} · ${esc(types[r.tipo] || r.tipo)}</span>${badge(r.status)}</div><h2>${esc(r.assunto)}</h2><p>${esc(r.solicitante)} · ${esc(date(r.criado_em, true))}</p><footer>${button("request-detail", "Ver detalhes", r.id)}${manager() && r.status === "ABERTA" ? button("answer-request", "Responder", r.id) : ""}${Number(r.usuario_id) === Number(context().usuario.id) && r.status === "ABERTA" ? button("cancel-request", "Cancelar solicitação", r.id) : ""}</footer></article>`,
        )
        .join("") ||
      empty(
        "Nenhuma solicitação encontrada.",
        "Abra um protocolo para iniciar um atendimento interno. Não há solicitações fictícias nesta lista.",
      )
    );
  }
  function complements(data) {
    cache.complements = data;
    const sent = data.reduce((s, r) => s + Number(r.horas), 0),
      approved = data.reduce((s, r) => s + Number(r.horas_aprovadas), 0);
    return (
      title(
        "Atividades complementares",
        "Envie uma referência de certificado e acompanhe a análise da gestão.",
        button("new-complement", "Adicionar atividade", "", "primary"),
      ) +
      `<section class="service-summary"><div><small>Horas enviadas</small><strong>${sent.toLocaleString("pt-BR")}h</strong></div><div><small>Horas aprovadas</small><strong>${approved.toLocaleString("pt-BR")}h</strong></div><p class="summary-note">A submissão não aprova horas automaticamente. Pareceres e horas aprovadas são registrados pela gestão nesta homologação.</p></section><div class="service-toolbar"><label class="service-filter">Status<select id="complement-status"><option value="">Todos</option>${["PENDENTE", "APROVADA", "REPROVADA"].map((s) => `<option value="${s}">${labels[s]}</option>`).join("")}</select></label></div><div id="complement-list">${complementTable(data)}</div>`
    );
  }
  function complementTable(data) {
    return data.length
      ? `<div class="service-table"><div class="table-wrap"><table><thead><tr><th>Certificado / aluno</th><th>Data</th><th>Situação</th><th>Enviadas</th><th>Aprovadas</th><th>Ações</th></tr></thead><tbody>${data.map((r) => `<tr><td><strong>${esc(r.titulo)}</strong><small>${esc(r.aluno_nome)}</small></td><td>${date(r.data)}</td><td>${badge(r.status)}</td><td>${Number(r.horas)}h</td><td>${Number(r.horas_aprovadas)}h</td><td><div class="row-actions">${button("complement-detail", "Detalhes", r.id, "small")}${manager() && r.status === "PENDENTE" ? button("review-complement", "Avaliar", r.id, "small") : ""}</div></td></tr>`).join("")}</tbody></table></div></div>`
      : empty(
          "Nenhuma atividade neste status.",
          "As horas só são contabilizadas como aprovadas depois da análise da gestão.",
        );
  }
  function timetable(data) {
    cache.schedules = data;
    const names = [
      "Segunda",
      "Terça",
      "Quarta",
      "Quinta",
      "Sexta",
      "Sábado",
      "Domingo",
    ];
    return (
      title(
        "Quadro de horários",
        "Grade semanal cadastrada pela gestão, respeitando seus vínculos de turma.",
        manager() ? button("new-schedule", "Novo horário", "", "primary") : "",
      ) +
      quick() +
      `<div class="service-toolbar"><label class="period-selector">Período<select id="schedule-period"><option value="">Todos os períodos</option>${[
        ...new Set(data.map((r) => r.periodo)),
      ]
        .sort()
        .reverse()
        .map((v) => `<option>${esc(v)}</option>`)
        .join(
          "",
        )}</select></label><span class="service-title-count">${data.length} horário(s) cadastrado(s)</span></div><div id="schedule-results">${scheduleGrid(data, names)}</div>`
    );
  }
  function scheduleGrid(
    data,
    names = [
      "Segunda",
      "Terça",
      "Quarta",
      "Quinta",
      "Sexta",
      "Sábado",
      "Domingo",
    ],
  ) {
    return data.length
      ? `<div class="schedule-scroll" tabindex="0" role="region" aria-label="Grade semanal: role horizontalmente para ver todos os dias"><div class="schedule-grid">${names
          .map(
            (name, i) =>
              `<section class="schedule-day"><h2>${name}</h2>${
                data
                  .filter((r) => Number(r.dia) === i + 1)
                  .map(
                    (r) =>
                      `<article class="schedule-slot"><small>${esc(String(r.inicio).slice(0, 5))} – ${esc(String(r.fim).slice(0, 5))}</small><strong>${esc(r.disciplina_nome)}</strong><small>${esc(r.professor_nome)}</small><small>${esc(r.sala || "Sala não informada")} · ${esc(r.periodo)}</small>${manager() ? button("cancel-schedule", "Desativar", r.id, "small") : ""}</article>`,
                  )
                  .join("") || '<small class="muted">Sem horários</small>'
              }</section>`,
          )
          .join("")}</div></div>`
      : empty(
          "Sem horários cadastrados.",
          "A gestão precisa informar os dias e horários das turmas. Nenhuma grade é inferida a partir do vídeo.",
        );
  }
  function calendar(data) {
    cache.events = data;
    const first = new Date(year, month, 1),
      days = new Date(year, month + 1, 0).getDate(),
      today = new Date(),
      key = (d) =>
        `${year}-${String(month + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    const byDay = (d) =>
      data.filter((r) => {
        const p = new Date(r.prazo);
        return (
          p.getFullYear() === year &&
          p.getMonth() === month &&
          p.getDate() === d
        );
      });
    return (
      title(
        "Calendário",
        "Prazos das atividades publicadas para as suas turmas, no horário do navegador.",
      ) +
      `<section class="panel calendar-panel"><div class="calendar-head">${button("previous-month", "Mês anterior")}<h2>${esc(new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" }).format(first))}</h2>${button("next-month", "Próximo mês")}</div><div class="calendar-grid">${["D", "S", "T", "Q", "Q", "S", "S"].map((x) => `<span>${x}</span>`).join("")}${Array.from({ length: first.getDay() }, () => "<span></span>").join("")}${Array.from(
        { length: days },
        (_, i) => {
          const d = i + 1,
            events = byDay(d);
          return `<button class="calendar-day ${events.length ? "has-event" : ""} ${today.toDateString() === new Date(year, month, d).toDateString() ? "today" : ""}" data-service="day-events" data-date="${key(d)}" aria-label="${date(key(d))}: ${events.length} prazo(s)"><span>${d}</span>${events.length ? `<small>${events.length} prazo(s)</small>` : ""}</button>`;
        },
      ).join(
        "",
      )}</div><div class="calendar-events"><h3>Atividades deste mês</h3>${eventRows(data.filter((r) => new Date(r.prazo).getMonth() === month && new Date(r.prazo).getFullYear() === year))}</div></section>`
    );
  }
  function eventRows(data) {
    return data.length
      ? data
          .map(
            (r) =>
              `<div class="event-row"><time>${date(r.prazo, true)}</time><div><strong>${esc(r.titulo)}</strong><p>${esc(r.turma_nome)}</p><a href="#atividades?turma=${Number(r.turma_id)}">Ver atividade</a></div></div>`,
          )
          .join("")
      : '<p class="dialog-help">Nenhum prazo publicado neste período.</p>';
  }
  function resources(data) {
    cache.materials = data;
    return (
      title(
        "Biblioteca e links",
        "Materiais publicados no portal e orientação sobre serviços externos.",
      ) +
      `<div class="notice">Teams, Office, biblioteca institucional e ambiente de provas exigem integração e credenciais próprias. Não há SSO institucional conectado nesta homologação.</div><div class="service-toolbar"><input type="search" id="material-search" class="service-search" aria-label="Buscar material" placeholder="Buscar material publicado"></div><section class="detail-stack" id="materials-list">${links(data) || empty("Nenhum material disponível.", "Materiais com referência HTTPS aparecem aqui depois de publicados pelo professor.")}</section><div class="service-quick-links">${button("integration-info", "Como acessar serviços externos?")}<a class="btn" href="#solicitacoes">Solicitar atendimento</a></div>`
    );
  }
  async function render(route) {
    if (!Object.hasOwn(routes, route)) return false;
    let data;
    switch (route) {
      case "meu-curso":
        data = await Promise.all([get("turmas"), get("aulas"), get("boletim")]);
        break;
      case "perfil":
        data = await get("perfil");
        break;
      case "solicitacoes":
        data = await get("solicitacoes");
        break;
      case "complementares":
        data = await get("complementares");
        break;
      case "horarios":
        data = await get("horarios");
        break;
      case "calendario":
        data = await get("atividades");
        break;
      case "recursos":
        data = isAcademic() ? await get("aulas") : [];
        break;
    }
    if (location.hash.slice(1).split("?")[0] !== route) return true;
    $("#conteudo").innerHTML = {
      "meu-curso": () => renderCourse(data),
      perfil: () => profile(data),
      solicitacoes: () => requests(data),
      complementares: () => complements(data),
      horarios: () => timetable(data),
      calendario: () => calendar(data),
      recursos: () => resources(data),
    }[route]();
    wire(route);
    return true;
  }
  function wire(route) {
    if (route === "meu-curso")
      $("#course-period").onchange = (e) =>
        (location.hash =
          "meu-curso?periodo=" + encodeURIComponent(e.target.value));
    if (route === "solicitacoes") {
      const draw = () => {
        const s = $("#request-status").value,
          q = $("#request-search").value.toLocaleLowerCase("pt-BR");
        $("#requests-list").innerHTML = requestCards(
          cache.requests.filter(
            (r) =>
              (!s || r.status === s) &&
              [r.id, r.assunto, r.solicitante]
                .join(" ")
                .toLocaleLowerCase("pt-BR")
                .includes(q),
          ),
        );
      };
      $("#request-status").onchange = draw;
      $("#request-search").oninput = draw;
    }
    if (route === "complementares")
      $("#complement-status").onchange = (e) =>
        ($("#complement-list").innerHTML = complementTable(
          cache.complements.filter(
            (r) => !e.target.value || r.status === e.target.value,
          ),
        ));
    if (route === "horarios")
      $("#schedule-period").onchange = (e) =>
        ($("#schedule-results").innerHTML = scheduleGrid(
          cache.schedules.filter(
            (r) => !e.target.value || r.periodo === e.target.value,
          ),
        ));
    if (route === "recursos")
      $("#material-search").oninput = (e) =>
        ($("#materials-list").innerHTML =
          links(
            cache.materials.filter((r) =>
              [r.titulo, r.conteudo]
                .join(" ")
                .toLowerCase()
                .includes(e.target.value.toLowerCase()),
            ),
          ) || empty("Nenhum material encontrado.", "Tente outro termo."));
  }
  async function editProfile() {
    const r = cache.profile;
    H.form(
      "Editar meus dados",
      [
        {
          name: "telefone",
          label: "Telefone",
          max: 25,
          value: r.telefone,
          required: false,
        },
        { name: "cep", label: "CEP", max: 12, value: r.cep, required: false },
        {
          name: "endereco",
          label: "Endereço",
          max: 240,
          value: r.endereco,
          required: false,
          wide: true,
        },
        ...[
          ["numero", "Número", 20],
          ["complemento", "Complemento", 120],
          ["bairro", "Bairro", 100],
          ["cidade", "Cidade", 100],
          ["estado", "UF (duas letras)", 2],
          ["escola", "Escola anterior", 160],
        ].map(([name, label, max]) => ({
          name,
          label,
          max,
          value: r[name],
          required: false,
        })),
        {
          name: "anoConclusao",
          label: "Ano de conclusão",
          type: "number",
          min: 1900,
          max: 2100,
          value: r.ano_conclusao,
          required: false,
        },
      ],
      (v) =>
        put("perfil", {
          ...v,
          estado: v.estado.toUpperCase(),
          anoConclusao: v.anoConclusao ? Number(v.anoConclusao) : null,
        }),
      {
        help: "Salvo apenas no seu cadastro e unidade. Nome, e-mail e permissões não são alterados por este formulário.",
        submit: "Salvar meus dados",
      },
    );
  }
  function newRequest() {
    H.form(
      "Nova solicitação",
      [
        {
          name: "tipo",
          label: "Tipo de solicitação",
          type: "select",
          options: options(types),
        },
        { name: "assunto", label: "Assunto", max: 160, wide: true },
        {
          name: "mensagem",
          label: "Descreva seu pedido",
          type: "textarea",
          max: 4000,
          wide: true,
        },
      ],
      (v) => post("solicitacoes", v),
      {
        help: "Protocolo interno de homologação. Não envia pedido a uma instituição externa nem emite documento oficial.",
        submit: "Enviar solicitação",
      },
    );
  }
  async function newComplement() {
    const student = context().usuario.perfil === "ALUNO";
    const people = student
      ? []
      : (await get("pessoas")).filter((p) => p.perfil === "ALUNO" && p.ativo);
    H.form(
      "Adicionar atividade complementar",
      [
        ...(!student
          ? [
              {
                name: "alunoId",
                label: "Aluno",
                type: "select",
                options: people.map((p) => ({ value: p.id, label: p.nome })),
                wide: true,
              },
            ]
          : []),
        {
          name: "titulo",
          label: "Título do certificado",
          max: 160,
          wide: true,
        },
        { name: "data", label: "Data da atividade", type: "date" },
        {
          name: "horas",
          label: "Horas enviadas",
          type: "number",
          min: 0.25,
          max: 1000,
          step: ".25",
        },
        {
          name: "comprovanteUrl",
          label: "Referência HTTPS do comprovante",
          type: "url",
          max: 2000,
          required: false,
          wide: true,
          help: "A referência não é baixada automaticamente. Não inclua links com senhas ou dados de acesso.",
        },
      ],
      (v) =>
        post("complementares", {
          ...v,
          alunoId: student ? null : Number(v.alunoId),
          horas: Number(v.horas),
        }),
      {
        help: "Envio para análise, sem aprovação automática. Esta versão recebe uma referência HTTPS, não um arquivo anexado.",
        submit: "Enviar para análise",
      },
    );
  }
  async function newSchedule() {
    const data = await get("turmas");
    H.form(
      "Novo horário",
      [
        {
          name: "turmaId",
          label: "Turma / período",
          type: "select",
          options: data.map((r) => ({
            value: r.id,
            label: r.nome + " · " + r.periodo,
          })),
          wide: true,
        },
        {
          name: "dia",
          label: "Dia da semana",
          type: "select",
          options: [
            "Segunda",
            "Terça",
            "Quarta",
            "Quinta",
            "Sexta",
            "Sábado",
            "Domingo",
          ].map((label, i) => ({ value: i + 1, label })),
        },
        { name: "sala", label: "Sala ou local", max: 100, required: false },
        { name: "inicio", label: "Início", type: "time" },
        { name: "fim", label: "Término", type: "time" },
      ],
      (v) =>
        post("horarios", {
          ...v,
          turmaId: Number(v.turmaId),
          dia: Number(v.dia),
        }),
      {
        help: "Horário semanal cadastrado, não importado de um sistema institucional.",
        submit: "Salvar horário",
      },
    );
  }
  async function notices() {
    show(
      "Avisos do portal",
      '<p class="dialog-help">Carregando registros…</p>',
    );
    try {
      const [requests, activities] = await Promise.all([
        get("solicitacoes"),
        isAcademic() ? get("atividades") : Promise.resolve([]),
      ]);
      if (!$("#service-dialog").open) return;
      $("#service-body").innerHTML =
        `<div class="detail-stack"><article><h3>Solicitações internas</h3><p>${requests.filter((r) => r.status === "ABERTA").length} aberta(s) · ${requests.filter((r) => r.status === "RESPONDIDA").length} respondida(s)</p><a href="#solicitacoes" data-service="close-service">Acompanhar solicitações</a></article>${isAcademic() ? `<article><h3>Prazos publicados</h3>${eventRows(activities.filter((r) => new Date(r.prazo) >= new Date()).slice(0, 8))}</article>` : ""}<article><h3>Ambiente independente</h3><p>Os avisos são derivados dos dados reais desta homologação, não de notificações institucionais externas.</p></article></div>`;
    } catch (e) {
      $("#service-body").textContent =
        e.message || "Não foi possível carregar os avisos.";
    }
  }
  const tourSteps = [
    [
      "Bem-vindo ao seu portal",
      "A navegação reúne ensino, serviços e atendimento. Os módulos respeitam o seu perfil e a unidade selecionada.",
    ],
    [
      "Seu semestre em um só lugar",
      "Em Meu curso, escolha um período e abra materiais e avaliações. O quadro de horários e o calendário usam apenas registros cadastrados.",
    ],
    [
      "Serviços com acompanhamento",
      "Solicitações recebem protocolo e resposta. Atividades complementares passam pela análise da gestão antes de contar horas aprovadas.",
    ],
    [
      "Do seu jeito, com segurança",
      "Alterne claro/escuro pelo botão de lua/sol. Meus dados edita contato e endereço; financeiro e carteirinha permanecem demonstrativos.",
    ],
  ];
  function tourDraw() {
    const d = $("#tour-dialog");
    $("#tour-step").textContent = `${step + 1} de ${tourSteps.length}`;
    d.querySelector("h2").textContent = tourSteps[step][0];
    d.querySelector("p").textContent = tourSteps[step][1];
    d.querySelector("[data-tour-next]").textContent =
      step === tourSteps.length - 1 ? "Concluir passeio" : "Próximo passo";
    d.querySelector("[data-tour-previous]").disabled = step === 0;
  }
  function tour() {
    tourFocus = document.activeElement;
    step = 0;
    tourDraw();
    $("#tour-dialog").showModal();
  }
  function overview() {
    return `<section class="welcome-strip"><div><h2>Encontre o que precisa.</h2><p>Conheça os módulos, os serviços e os recursos do portal.</p></div><div class="service-quick-links">${isAcademic() ? '<a class="btn primary" href="#meu-curso">Meu curso</a>' : ""}${button("tour", "Conhecer o portal")}</div></section>`;
  }
  function init(hooks) {
    H = hooks;
    const d = document.createElement("dialog");
    d.id = "service-dialog";
    d.setAttribute("aria-labelledby", "service-title");
    d.innerHTML = `<div class="dialog-heading"><h2 id="service-title">Detalhes</h2><button class="icon-btn" data-service="close-service" aria-label="Fechar detalhes">${I("close")}</button></div><div id="service-body"></div>`;
    document.body.append(d);
    d.addEventListener("close", () => modalFocus?.focus());
    const t = document.createElement("dialog");
    t.id = "tour-dialog";
    t.setAttribute("aria-labelledby", "tour-title");
    t.innerHTML = `<button class="icon-btn tour-close" aria-label="Fechar passeio">${I("close")}</button><span class="tour-progress" id="tour-step"></span><h2 id="tour-title"></h2><p></p><div class="tour-actions"><button class="btn" data-tour-previous>Passo anterior</button><button class="btn primary" data-tour-next>Próximo passo</button></div>`;
    document.body.append(t);
    t.addEventListener("close", () => tourFocus?.focus());
    t.querySelector(".tour-close").onclick = () => t.close();
    t.querySelector("[data-tour-next]").onclick = () => {
      if (step === tourSteps.length - 1) t.close();
      else {
        step++;
        tourDraw();
      }
    };
    t.querySelector("[data-tour-previous]").onclick = () => {
      step = Math.max(0, step - 1);
      tourDraw();
    };
    const tools = $(".topbar-tools");
    tools.insertAdjacentHTML(
      "afterbegin",
      `<button class="icon-btn" data-service="notices" aria-label="Abrir avisos" title="Avisos">${I("bell")}</button><a class="icon-btn" href="#perfil" aria-label="Meu perfil" title="Meu perfil">${I("user")}</a>`,
    );
    document.addEventListener("click", async (e) => {
      const target = e.target.closest("[data-service]");
      if (!target) return;
      const action = target.dataset.service,
        id = Number(target.dataset.serviceId);
      target.disabled = true;
      try {
        switch (action) {
          case "close-service":
            close();
            break;
          case "tour":
            tour();
            break;
          case "notices":
            await notices();
            break;
          case "edit-profile":
            await editProfile();
            break;
          case "new-request":
            newRequest();
            break;
          case "new-complement":
            await newComplement();
            break;
          case "new-schedule":
            await newSchedule();
            break;
          case "request-detail": {
            const r = cache.requests.find((x) => Number(x.id) === id);
            show(
              "Solicitação #" + id,
              `<div class="detail-stack"><article><h3>${esc(r.assunto)}</h3><p>${esc(r.mensagem)}</p><footer>${badge(r.status)}</footer></article>${r.resposta ? `<article><h3>Resposta da gestão</h3><p>${esc(r.resposta)}</p><footer>${date(r.respondido_em, true)}</footer></article>` : '<p class="dialog-help">Ainda não há resposta registrada.</p>'}</div>`,
            );
            break;
          }
          case "answer-request":
            H.form(
              "Responder solicitação #" + id,
              [
                {
                  name: "resposta",
                  label: "Resposta para o solicitante",
                  type: "textarea",
                  max: 4000,
                  wide: true,
                },
              ],
              (v) => put("solicitacoes/" + id + "/resposta", v),
              { submit: "Registrar resposta" },
            );
            break;
          case "cancel-request":
            if (
              await PortalUX.confirm(
                "Cancelar este protocolo interno? O histórico será preservado.",
                {
                  accept: "Cancelar solicitação",
                  cancel: "Manter solicitação",
                },
              )
            ) {
              await post("solicitacoes/" + id + "/cancelar");
              await H.reload();
            }
            break;
          case "complement-detail": {
            const r = cache.complements.find((x) => Number(x.id) === id);
            show(
              "Atividade complementar",
              `<div class="detail-stack"><article><h3>${esc(r.titulo)}</h3><p>${date(r.data)} · ${Number(r.horas)}h enviadas · ${Number(r.horas_aprovadas)}h aprovadas</p><footer>${badge(r.status)}</footer>${safe(r.comprovante_url) ? `<a href="${esc(safe(r.comprovante_url))}" target="_blank" rel="noopener noreferrer">Abrir referência do comprovante ↗</a>` : "<p>Sem referência HTTPS informada.</p>"}</article><article><h3>Parecer</h3><p>${esc(r.parecer || "Aguardando análise da gestão.")}</p></article></div>`,
            );
            break;
          }
          case "review-complement": {
            const r = cache.complements.find((x) => Number(x.id) === id);
            H.form(
              "Avaliar atividade complementar",
              [
                {
                  name: "status",
                  label: "Resultado",
                  type: "select",
                  options: [
                    { value: "APROVADA", label: "Aprovar" },
                    { value: "REPROVADA", label: "Reprovar" },
                  ],
                },
                {
                  name: "horasAprovadas",
                  label: "Horas aprovadas",
                  type: "number",
                  min: 0,
                  max: Number(r.horas),
                  step: ".25",
                  value: r.horas,
                },
                {
                  name: "parecer",
                  label: "Parecer para o aluno",
                  type: "textarea",
                  max: 2000,
                  wide: true,
                },
              ],
              (v) =>
                put("complementares/" + id + "/avaliacao", {
                  ...v,
                  horasAprovadas: Number(v.horasAprovadas),
                }),
              {
                help:
                  r.titulo +
                  " · ao reprovar, informe zero horas aprovadas. A análise será registrada no histórico.",
                submit: "Registrar análise",
              },
            );
            break;
          }
          case "cancel-schedule":
            if (
              await PortalUX.confirm(
                "Desativar este horário? O registro será preservado no histórico.",
                { accept: "Desativar horário" },
              )
            ) {
              await post("horarios/" + id + "/cancelar");
              await H.reload();
            }
            break;
          case "materials": {
            const rows = cache.course.aulas.filter(
              (a) => Number(a.turma_id) === id,
            );
            show(
              "Materiais da disciplina",
              `<div class="detail-stack">${links(rows) || '<p class="dialog-help">Nenhum material publicado para esta turma.</p>'}</div>`,
            );
            break;
          }
          case "grades": {
            const rows = cache.course.grades.filter(
              (g) => Number(g.turma_id) === id,
            );
            show(
              "Avaliações publicadas",
              `<div class="detail-stack">${rows.map((g) => `<article><h3>${esc(g.atividade_titulo)}</h3><p>Nota: ${Number(g.nota).toLocaleString("pt-BR")} / 10</p>${manager() ? `<p>${esc(g.aluno_nome)}</p>` : ""}<p>${esc(g.feedback || "Sem observações.")}</p></article>`).join("") || '<p class="dialog-help">Nenhuma avaliação publicada para esta turma.</p>'}<p class="dialog-help">Resultados individuais, sem cálculo de média ou aprovação institucional.</p></div>`,
            );
            break;
          }
          case "previous-month":
            month--;
            if (month < 0) {
              month = 11;
              year--;
            }
            await H.reload();
            break;
          case "next-month":
            month++;
            if (month > 11) {
              month = 0;
              year++;
            }
            await H.reload();
            break;
          case "day-events":
            show(
              "Prazos de " + date(target.dataset.date),
              eventRows(
                cache.events.filter(
                  (r) =>
                    new Date(r.prazo).toDateString() ===
                    new Date(target.dataset.date + "T12:00:00").toDateString(),
                ),
              ),
            );
            break;
          case "integration-info":
            show(
              "Serviços externos",
              `<p class="dialog-help">Teams, Office, biblioteca institucional e provas externas não estão integrados. Não copie credenciais de outro sistema para cá. Para cadastrar material ou solicitar acesso, use os fluxos internos e a administração da unidade.</p>`,
            );
            break;
        }
      } catch (err) {
        H.notify(err.message || "Não foi possível concluir a operação.", true);
      } finally {
        target.disabled = false;
      }
    });
  }
  return { routes, init, render, overview };
})();
