/* global Api, Form */
(() => {
  "use strict";
  const $ = (q, root = document) => root.querySelector(q);
  const esc = Form.esc;
  const base = "/api/academico";
  const roles = {
    ADMIN: "Administração",
    COORDENACAO: "Coordenação",
    PROFESSOR: "Professor",
    ALUNO: "Aluno",
    FINANCEIRO: "Financeiro",
    RECEPCAO: "Recepção",
  };
  let ctx,
    current = "inicio",
    rows = [],
    query = "",
    page = 1,
    dirty = false,
    returnFocus = null,
    renderSequence = 0;
  const dialog = $("#entity-dialog");
  const academic = ["ADMIN", "COORDENACAO", "PROFESSOR", "ALUNO"];
  const managers = ["ADMIN", "COORDENACAO"];
  const teachers = ["ADMIN", "COORDENACAO", "PROFESSOR"];
  const routes = {
    inicio: {
      name: "Visão geral",
      group: "SEU PORTAL",
      roles: Object.keys(roles),
      icon: "home",
    },
    pessoas: {
      name: "Pessoas e acessos",
      group: "GESTÃO",
      roles: ["ADMIN", "COORDENACAO", "FINANCEIRO"],
      icon: "people",
      desc: "Cadastros e permissões, organizados por unidade.",
      action: "Nova pessoa",
    },
    cursos: {
      name: "Cursos",
      group: "GESTÃO",
      roles: managers,
      icon: "book",
      desc: "Organize os cursos e suas cargas horárias.",
      action: "Novo curso",
    },
    disciplinas: {
      name: "Disciplinas",
      group: "GESTÃO",
      roles: managers,
      icon: "layers",
      desc: "Componentes curriculares vinculados aos cursos.",
      action: "Nova disciplina",
    },
    turmas: {
      name: "Turmas",
      group: "ENSINO",
      roles: academic,
      icon: "people",
      desc: "Disciplinas, docentes e vagas em um só lugar.",
      action: "Nova turma",
    },
    matriculas: {
      name: "Matrículas",
      group: "ENSINO",
      roles: academic,
      icon: "check",
      desc: "Acompanhe os vínculos dos alunos com cada turma.",
      action: "Nova matrícula",
    },
    aulas: {
      name: "Aulas e materiais",
      group: "ENSINO",
      roles: academic,
      icon: "book",
      desc: "Conteúdo publicado pelos professores das suas turmas.",
      action: "Publicar aula",
    },
    atividades: {
      name: "Atividades",
      group: "ENSINO",
      roles: academic,
      icon: "clipboard",
      desc: "Enunciados, prazos e entregas de cada turma.",
      action: "Nova atividade",
    },
    entregas: {
      name: "Entregas",
      group: "ENSINO",
      roles: academic,
      icon: "upload",
      desc: "Respostas enviadas e avaliações individuais.",
    },
    boletim: {
      name: "Notas e boletim",
      group: "SERVIÇOS",
      roles: academic,
      icon: "chart",
      desc: "Somente avaliações publicadas. Sem média ou aprovação institucional automática.",
    },
    financeiro: {
      name: "Financeiro",
      group: "SERVIÇOS",
      roles: ["ADMIN", "FINANCEIRO", "ALUNO"],
      icon: "wallet",
      desc: "Extrato de lançamentos e baixas exclusivamente demonstrativos.",
      action: "Novo lançamento",
    },
    carteirinha: {
      name: "Carteirinha",
      group: "SERVIÇOS",
      roles: ["ADMIN", "COORDENACAO", "ALUNO"],
      icon: "card",
      desc: "Identificação demonstrativa de estudantes com matrícula ativa.",
    },
    auditoria: {
      name: "Histórico de ações",
      group: "ADMINISTRAÇÃO",
      roles: ["ADMIN"],
      icon: "shield",
      desc: "Últimas 200 operações desta unidade, com responsável e horário.",
    },
  };
  const paths = {
    home: "M3 10 10 4l7 6v7a1 1 0 0 1-1 1h-4v-6H8v6H4a1 1 0 0 1-1-1z",
    people:
      "M14 18v-2a4 4 0 0 0-8 0v2M10 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6M16 6a3 3 0 0 1 0 6M17 18v-2a4 4 0 0 0-2-3",
    book: "M10 5C7 3 4 3 2 4v13c3-1 5-1 8 1 3-2 5-2 8-1V4c-2-1-5-1-8 1v13",
    layers: "m10 2 8 4-8 4-8-4zM2 10l8 4 8-4M2 14l8 4 8-4",
    check:
      "M6 3H4a1 1 0 0 0-1 1v13a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1h-2M7 2h6v4H7zM6 12l3 3 5-6",
    clipboard: "M7 3H4v15h12V3h-3M7 2h6v4H7zM6 10h8M6 14h5",
    upload: "M10 13V3M6 7l4-4 4 4M3 12v5h14v-5",
    chart: "M3 3v14h15M7 13V9M11 13V5M15 13v-3",
    wallet: "M17 6V3H3v14h14v-6h-5V6zM12 7h6v4h-6z",
    card: "M2 5h16v11H2zM2 8h16M5 12h4",
    shield: "M10 2l7 3v5c0 4-4 7-7 8-3-1-7-4-7-8V5zM6 10l3 3 5-6",
    search: "M9 15a6 6 0 1 0 0-12 6 6 0 0 0 0 12m5-1 4 4",
  };
  function icon(name) {
    return `<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.45" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${paths[name] || paths.book}"/></svg>`;
  }
  function is(...rs) {
    return rs.includes(ctx.usuario.perfil);
  }
  function allowed(route) {
    return (
      Object.hasOwn(routes, route) &&
      routes[route].roles.includes(ctx.usuario.perfil)
    );
  }
  function canCreate(route) {
    return route === "pessoas"
      ? is(...managers)
      : route === "financeiro"
        ? is("ADMIN", "FINANCEIRO")
        : ["aulas", "atividades"].includes(route)
          ? is(...teachers)
          : is(...managers);
  }

  function state() {
    const parts = location.hash.slice(1).split("?");
    return {
      route: parts[0] || "inicio",
      params: new URLSearchParams(parts.slice(1).join("?")),
    };
  }
  function go(route) {
    location.hash = route;
  }
  function shortDate(value, time = false) {
    if (!value) return "—";
    const date = new Date(
      time ? value : ("" + value).slice(0, 10) + "T12:00:00",
    );
    return Number.isNaN(date.valueOf())
      ? "—"
      : new Intl.DateTimeFormat(
          "pt-BR",
          time
            ? { dateStyle: "short", timeStyle: "short" }
            : { dateStyle: "short" },
        ).format(date);
  }
  function money(v) {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(Number(v) || 0);
  }
  function badge(value, type = "neutral") {
    return `<span class="badge ${type}">${esc(value)}</span>`;
  }
  function notify(text, error = false) {
    const n = $("#notification");
    n.textContent = text;
    n.className = "notification" + (error ? " error" : "");
    n.hidden = false;
    clearTimeout(n._timer);
    n._timer = setTimeout(() => {
      n.hidden = true;
    }, 5500);
  }
  async function req(method, route, body) {
    try {
      return await Api[method.toLowerCase()](base + route, body);
    } catch (e) {
      if (e.status === 401) location.replace("/index.html?expirou=1");
      throw e;
    }
  }
  function heading(title, desc, action = "", eyebrow = "PORTAL INSTITUCIONAL") {
    return `<div class="page-heading"><div><p class="eyebrow">${esc(eyebrow)}</p><h1>${esc(title)}</h1>${desc ? `<p class="subtitle">${esc(desc)}</p>` : ""}</div>${action ? `<div class="heading-actions">${action}</div>` : ""}</div>`;
  }
  function empty(title, desc) {
    return `<div class="empty">${icon(routes[current]?.icon || "book")}<h2>${esc(title)}</h2><p>${esc(desc)}</p></div>`;
  }
  function navigation() {
    let last = "";
    $("#menu").innerHTML =
      Object.entries(routes)
        .filter(([key]) => allowed(key))
        .map(([key, r]) => {
          const group =
            r.group !== last
              ? `<p class="nav-caption">${esc(r.group)}</p>`
              : "";
          last = r.group;
          return (
            group +
            `<a class="nav-link" href="#${key}"${current === key ? ' aria-current="page"' : ""}>${icon(r.icon)}<span>${esc(key === "matriculas" && is("ALUNO") ? "Minha matrícula" : r.name)}</span></a>`
          );
        })
        .join("") +
      (ctx.clinicaPermitida
        ? `<p class="nav-caption">CLÍNICA</p><a class="nav-link" href="/dashboard.html">${icon("shield")}<span>Gestão clínica</span><span class="nav-arrow">↗</span></a>`
        : "");
  }
  function menu(open) {
    const sidebar = $("#sidebar");
    const wasInside = sidebar.contains(document.activeElement);
    const mobile = matchMedia("(max-width:780px)").matches;
    sidebar.classList.toggle("open", open);
    sidebar.inert = mobile && !open;
    sidebar.setAttribute("aria-hidden", String(mobile && !open));
    $("#nav-overlay").hidden = !open;
    $("#menu-toggle").setAttribute("aria-expanded", String(open));
    document.body.classList.toggle("menu-open", open);
    if (open) $("#menu a")?.focus();
    else if (mobile && wasInside) $("#menu-toggle").focus();
  }
  function syncSearch() {
    const s = state();
    if (query) s.params.set("busca", query);
    else s.params.delete("busca");
    if (page > 1) s.params.set("pagina", String(page));
    else s.params.delete("pagina");
    history.replaceState(
      null,
      "",
      "#" + s.route + (s.params.size ? "?" + s.params : ""),
    );
  }
  function shellTable() {
    return `<section class="panel"><div class="toolbar"><label class="search">${icon("search")}<input type="search" id="table-search" aria-label="Buscar nesta lista" placeholder="Buscar nesta lista…" value="${esc(query)}" autocomplete="off"></label><span class="toolbar-meta">Registros desta unidade · até 1.000 por consulta</span></div><div id="table-results"></div></section>`;
  }
  function columns() {
    const actions = (items) =>
      `<div class="row-actions">${items.join("")}</div>`;
    const btn = (action, id, label, extra = "") =>
      `<button class="btn small ${extra}" data-action="${action}" data-id="${Number(id)}">${esc(label)}</button>`;
    return (
      {
        pessoas: [
          [
            "Pessoa",
            (r) =>
              `<strong>${esc(r.nome)}</strong><small>${esc(r.email)}</small>`,
          ],
          ["Perfil", (r) => badge(roles[r.perfil] || r.perfil)],
          [
            "Acesso",
            (r) =>
              badge(r.ativo ? "Ativo" : "Suspenso", r.ativo ? "green" : "red"),
          ],
          [
            "",
            (r) =>
              is(...managers) ? actions([btn("edit", r.id, "Gerenciar")]) : "",
          ],
        ],
        cursos: [
          [
            "Curso",
            (r) =>
              `<strong>${esc(r.nome)}</strong><small>${esc(r.codigo)}</small>`,
          ],
          ["Carga horária", (r) => esc(r.carga_horaria) + " h"],
          [
            "Status",
            (r) =>
              badge(
                r.ativo ? "Ativo" : "Inativo",
                r.ativo ? "green" : "neutral",
              ),
          ],
          ["", (r) => actions([btn("edit", r.id, "Editar")])],
        ],
        disciplinas: [
          [
            "Disciplina",
            (r) =>
              `<strong>${esc(r.nome)}</strong><small>${esc(r.codigo)}</small>`,
          ],
          ["Curso", (r) => esc(r.curso_nome)],
          ["Carga horária", (r) => esc(r.carga_horaria) + " h"],
          ["", (r) => actions([btn("edit", r.id, "Editar")])],
        ],
        turmas: [
          [
            "Turma",
            (r) =>
              `<strong>${esc(r.nome)}</strong><small>${esc(r.periodo)} · ${esc(r.curso_nome)}</small>`,
          ],
          ["Disciplina", (r) => esc(r.disciplina_nome)],
          ["Professor", (r) => esc(r.professor_nome)],
          ["Matrículas", (r) => `${Number(r.alunos)} / ${Number(r.vagas)}`],
          [
            "",
            (r) =>
              actions([
                btn("open-turma", r.id, "Aulas"),
                ...(is(...managers) ? [btn("edit", r.id, "Editar")] : []),
              ]),
          ],
        ],
        matriculas: [
          [
            "Aluno",
            (r) =>
              `<strong>${esc(r.aluno_nome)}</strong><small>${esc(r.aluno_email)}</small>`,
          ],
          ["Turma", (r) => esc(r.turma_nome)],
          ["Matrícula", (r) => `#${Number(r.id)}`],
          [
            "Status",
            (r) =>
              badge(
                r.status === "ATIVA" ? "Ativa" : "Cancelada",
                r.status === "ATIVA" ? "green" : "neutral",
              ),
          ],
          [
            "",
            (r) =>
              is(...managers) && r.status === "ATIVA"
                ? actions([btn("cancel-enrolment", r.id, "Cancelar", "danger")])
                : "",
          ],
        ],
        entregas: [
          [
            "Atividade",
            (r) =>
              `<strong>${esc(r.atividade_titulo)}</strong><small>${esc(r.turma_nome)}</small>`,
          ],
          ["Aluno", (r) => esc(r.aluno_nome)],
          ["Enviado em", (r) => esc(shortDate(r.enviado_em, true))],
          [
            "Avaliação",
            (r) =>
              r.publicada
                ? badge("Publicada", "green")
                : r.nota != null && !is("ALUNO")
                  ? badge("Rascunho", "amber")
                  : badge("Aguardando"),
          ],
          [
            "Nota",
            (r) =>
              r.nota == null
                ? "—"
                : esc(
                    new Intl.NumberFormat("pt-BR", {
                      maximumFractionDigits: 2,
                    }).format(r.nota),
                  ),
          ],
          [
            "",
            (r) =>
              actions([
                btn("read-delivery", r.id, "Ler"),
                ...(!is("ALUNO") ? [btn("grade", r.id, "Avaliar")] : []),
              ]),
          ],
        ],
        boletim: [
          [
            "Disciplina",
            (r) =>
              `<strong>${esc(r.disciplina_nome)}</strong><small>${esc(r.turma_nome)} · ${esc(r.periodo)}</small>`,
          ],
          ["Aluno", (r) => esc(r.aluno_nome)],
          ["Atividade", (r) => esc(r.atividade_titulo)],
          [
            "Nota / 10",
            (r) =>
              `<strong>${esc(new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 2 }).format(r.nota))}</strong>`,
          ],
          ["Feedback", (r) => esc(r.feedback || "Sem observações.")],
        ],
        financeiro: [
          [
            "Aluno / Descrição",
            (r) =>
              `<strong>${esc(r.aluno_nome)}</strong><small>${esc(r.descricao)}</small>`,
          ],
          ["Vencimento", (r) => esc(shortDate(r.vencimento))],
          ["Valor simulado", (r) => money(r.valor)],
          [
            "Situação",
            (r) =>
              badge(
                r.status === "ABERTO" ? "Em aberto" : "Baixa demonstrativa",
                r.status === "ABERTO" ? "amber" : "green",
              ),
          ],
          [
            "",
            (r) =>
              is("ADMIN", "FINANCEIRO") && r.status === "ABERTO"
                ? actions([btn("settle-demo", r.id, "Simular baixa")])
                : "",
          ],
        ],
        auditoria: [
          [
            "Operação",
            (r) =>
              `<strong>${esc(r.acao.replaceAll("_", " "))}</strong><small>${esc(r.entidade)} #${Number(r.registro_id)}</small>`,
          ],
          ["Responsável", (r) => esc(r.ator_nome)],
          ["Data e hora", (r) => esc(shortDate(r.criado_em, true))],
        ],
      }[current] || []
    );
  }
  function drawTable() {
    const cols = columns();
    let list = rows.filter((r) =>
      Object.values(r)
        .join(" ")
        .toLocaleLowerCase("pt-BR")
        .includes(query.toLocaleLowerCase("pt-BR")),
    );
    const pages = Math.max(1, Math.ceil(list.length / 12));
    page = Math.min(Math.max(1, page), pages);
    const slice = list.slice((page - 1) * 12, page * 12);
    $("#table-results").innerHTML = list.length
      ? `<div class="table-wrap"><table><thead><tr>${cols.map(([label]) => `<th scope="col">${esc(label || "Ações")}</th>`).join("")}</tr></thead><tbody>${slice.map((r) => `<tr>${cols.map(([, render]) => `<td>${render(r)}</td>`).join("")}</tr>`).join("")}</tbody></table></div><div class="pagination"><span>${list.length} ${list.length === 1 ? "registro" : "registros"} · página ${page} de ${pages}</span><div><button class="btn small" data-page="${page - 1}" ${page === 1 ? "disabled" : ""} aria-label="Página anterior">←</button><button class="btn small" data-page="${page + 1}" ${page === pages ? "disabled" : ""} aria-label="Próxima página">→</button></div></div>`
      : empty(
          query
            ? "Nenhum resultado para esta busca."
            : "Ainda não há registros.",
          query
            ? "Tente outro nome, código ou termo."
            : "Os registros aparecerão aqui depois de cadastrados.",
        );
  }
  function wireTable() {
    drawTable();
    $("#table-search")?.addEventListener("input", (e) => {
      query = e.target.value;
      page = 1;
      syncSearch();
      drawTable();
    });
  }
  async function overview(seq) {
    const role = ctx.usuario.perfil;
    let turmas = [],
      atividades = [],
      financeiro = [];
    if (academic.includes(role)) {
      [turmas, atividades] = await Promise.all([
        req("get", "/turmas"),
        req("get", "/atividades"),
      ]);
    }
    if (role === "FINANCEIRO") financeiro = await req("get", "/financeiro");
    if (seq !== renderSequence) return;
    const greeting = is("PROFESSOR")
      ? "Ensinar, acompanhar, desenvolver."
      : is("ALUNO")
        ? "Seu aprendizado, em um só lugar."
        : is("RECEPCAO")
          ? "A clínica começa com um bom atendimento."
          : "Visão geral do campus";
    const data = new Intl.DateTimeFormat("pt-BR", {
      day: "numeric",
      month: "long",
      year: "numeric",
    }).format(new Date());
    const stats = Object.entries(ctx.indicadores)
      .map(
        ([key, v]) =>
          `<div class="metric"><span>${esc({ cursos: "Cursos", disciplinas: "Disciplinas", turmas: is("ALUNO", "PROFESSOR") ? "Suas turmas" : "Turmas", matriculas: "Matrículas ativas", lancamentos: "Lançamentos simulados" }[key] || key)}</span><strong>${Number(v)}</strong><small>Registros persistidos nesta unidade</small></div>`,
      )
      .join("");
    const atividadeItems = atividades
      .filter((a) => new Date(a.prazo) >= new Date())
      .slice(0, 4)
      .map(
        (a) =>
          `<div class="list-item"><span class="list-marker">${icon("clipboard")}</span><div><strong>${esc(a.titulo)}</strong><p>${esc(a.turma_nome)} · até ${esc(shortDate(a.prazo, true))}</p></div>${badge(is("ALUNO") && a.entregue ? "Enviada" : "Publicada", is("ALUNO") && a.entregue ? "green" : "neutral")}</div>`,
      )
      .join("");
    const turmaItems = turmas
      .slice(0, 4)
      .map(
        (t) =>
          `<div class="list-item"><span class="list-marker">${icon("book")}</span><div><strong>${esc(t.nome)}</strong><p>${esc(t.disciplina_nome)} · ${esc(t.professor_nome)}</p></div><a class="btn small" href="#aulas?turma=${Number(t.id)}">Abrir</a></div>`,
      )
      .join("");
    $("#conteudo").innerHTML =
      heading(
        greeting,
        `Olá, ${ctx.usuario.nome.split(" ")[0]}. ${data}.`,
        "",
        roles[role].toLocaleUpperCase("pt-BR"),
      ) +
      window.PortalExperience.hero(ctx) +
      (stats
        ? `<section class="metrics" aria-label="Indicadores do portal">${stats}</section>`
        : "") +
      window.PortalExperience.shortcuts(ctx) +
      (academic.includes(role)
        ? `<div class="overview-grid"><section class="panel"><div class="section-heading"><div><h2>${is("ALUNO", "PROFESSOR") ? "Suas turmas" : "Turmas em andamento"}</h2><p>Acesso ao conteúdo e à rotina de ensino</p></div><a href="#turmas">Ver todas ↗</a></div>${turmaItems || empty("Nenhuma turma por aqui.", "A administração pode cadastrar turmas e vincular os participantes.")}</section><section class="panel"><div class="section-heading"><div><h2>Próximas atividades</h2><p>Prazos publicados pelos professores</p></div><a href="#atividades">Ver todas ↗</a></div>${atividadeItems || empty("Sem próximos prazos.", "As atividades publicadas aparecerão aqui.")}</section></div>`
        : is("RECEPCAO")
          ? `<section class="panel"><div class="empty">${icon("shield")}<h2>Gestão clínica preservada.</h2><p>Consulte pacientes, especialidades e atendimentos na área exclusiva da clínica. Seu perfil não recebe acesso aos dados acadêmicos.</p><a class="btn primary" href="/dashboard.html">Abrir gestão clínica ↗</a></div></section>`
          : `<section class="panel"><div class="section-heading"><h2>Financeiro demonstrativo</h2><a href="#financeiro">Abrir extrato ↗</a></div><div class="summary-strip"><span>Lançamentos<strong>${financeiro.length}</strong></span><span>Em aberto, valor simulado<strong>${money(financeiro.filter((f) => f.status === "ABERTO").reduce((s, f) => s + Math.round(Number(f.valor) * 100), 0) / 100)}</strong></span></div><div class="empty"><h2>Nenhum valor é cobrado por este portal.</h2><p>Lançamentos e baixas servem exclusivamente à homologação do fluxo financeiro.</p><a class="btn primary" href="#financeiro">Gerenciar lançamentos</a></div></section>`);
    $("#conteudo").insertAdjacentHTML(
      "beforeend",
      window.PortalExperience.discovery(ctx),
    );
    window.PortalExperience.mountOverview();
  }
  function safeLink(value) {
    try {
      const u = new URL(value);
      return u.protocol === "https:" && !u.username && !u.password
        ? u.href
        : null;
    } catch {
      return null;
    }
  }
  function cards() {
    if (!rows.length)
      return `<section class="panel">${empty("Nenhum conteúdo publicado.", "O professor responsável pode publicar conteúdo para esta turma.")}</section>`;
    return `<div class="content-grid">${rows
      .map((r) => {
        const isActivity = current === "atividades";
        const ended = isActivity && new Date(r.prazo) < new Date();
        const url = !isActivity && safeLink(r.material_url);
        return `<article class="content-card"><div class="card-meta"><span>${esc(r.turma_nome)}</span>${isActivity ? badge(is("ALUNO") && r.entregue ? "Entregue" : ended ? "Prazo encerrado" : "Disponível", is("ALUNO") && r.entregue ? "green" : ended ? "neutral" : "green") : `<time datetime="${esc(r.criado_em)}">${esc(shortDate(r.criado_em))}</time>`}</div><h2>${esc(r.titulo)}</h2><div class="prose">${esc(isActivity ? r.instrucoes : r.conteudo)}</div><div class="card-footer">${isActivity ? `<span class="deadline">Prazo de entrega<strong>${esc(shortDate(r.prazo, true))}</strong></span>` : url ? `<a href="${esc(url)}" target="_blank" rel="noopener noreferrer">Abrir material HTTPS ↗</a>` : '<span class="deadline">Conteúdo publicado no portal</span>'}<div class="row-actions">${isActivity ? (is("ALUNO") ? `<button class="btn small primary" data-action="submit-delivery" data-id="${Number(r.id)}" ${ended || r.entregue ? "disabled" : ""}>${r.entregue ? "Enviado" : "Enviar resposta"}</button>` : `<button class="btn small" data-action="open-deliveries" data-id="${Number(r.id)}">Entregas</button>`) : ""}${is(...teachers) ? `<button class="btn small" data-action="edit" data-id="${Number(r.id)}">Editar</button>` : ""}</div></div></article>`;
      })
      .join("")}</div>`;
  }
  async function identity(seq) {
    let people = [];
    const s = state();
    if (!is("ALUNO"))
      people = (await req("get", "/pessoas")).filter(
        (p) => p.perfil === "ALUNO" && p.ativo,
      );
    if (seq !== renderSequence) return;
    const selected = is("ALUNO")
      ? ctx.usuario.id
      : Number(s.params.get("aluno"));
    let card = "",
      error = "";
    if (selected) {
      try {
        card = await req(
          "get",
          "/carteirinha" + (is("ALUNO") ? "" : "?alunoId=" + selected),
        );
      } catch (e) {
        if (e.status !== 404) throw e;
        error = e.message;
      }
    }
    if (seq !== renderSequence) return;
    $("#conteudo").innerHTML =
      heading(
        "Carteirinha demonstrativa",
        routes.carteirinha.desc,
        card
          ? '<button class="btn" data-action="print">Imprimir demonstração</button>'
          : "",
      ) +
      '<div class="notice warning"><strong>Sem validade oficial.</strong> Não substitui documento institucional, não comprova direito à meia-entrada e não valida matrícula em sistemas externos.</div>' +
      (!is("ALUNO")
        ? `<div class="selector-bar"><label for="card-student">Selecionar aluno<select id="card-student"><option value="">Selecione um aluno…</option>${people.map((p) => `<option value="${Number(p.id)}" ${p.id === selected ? "selected" : ""}>${esc(p.nome)}</option>`).join("")}</select></label></div>`
        : "") +
      (card
        ? `<section class="identity-card" aria-label="Carteirinha sem validade oficial"><div class="identity-top"><img class="identity-brand" src="/img/uninassau-brasao.png" width="38" height="49" alt="Brasão UNINASSAU"><strong>UNINASSAU · Clínica-Escola</strong><span>IDENTIFICAÇÃO DEMONSTRATIVA</span></div><h2 class="identity-name">${esc(card.nome)}</h2><p class="identity-label">IDENTIFICADOR INTERNO</p><p class="identity-value">${esc(card.identificador)}</p><p class="identity-label">CURSOS COM MATRÍCULA ATIVA</p><p class="identity-value">${card.cursos.map((c) => esc(c.nome)).join(" · ")}</p><p class="identity-value">${esc(card.unidade)}</p><span class="identity-warning">DEMONSTRAÇÃO · SEM VALIDADE OFICIAL</span></section>`
        : `<section class="panel">${empty(error ? "Carteirinha indisponível." : "Selecione um aluno.", error || "É necessário ter pelo menos uma matrícula ativa nesta unidade.")}</section>`);
    $("#card-student")?.addEventListener("change", (e) =>
      go(
        "carteirinha" +
          (e.target.value ? "?aluno=" + Number(e.target.value) : ""),
      ),
    );
  }
  async function render() {
    const seq = ++renderSequence;
    const s = state();
    current = s.route;
    query = s.params.get("busca") || "";
    page = Number(s.params.get("pagina")) || 1;
    menu(false);
    navigation();
    const main = $("#conteudo");
    main.setAttribute("aria-busy", "true");
    if (!allowed(current)) {
      main.innerHTML =
        heading(
          "Acesso não permitido.",
          "Este módulo não está disponível para o seu perfil.",
        ) + '<a class="btn" href="#inicio">Voltar ao início</a>';
      main.setAttribute("aria-busy", "false");
      return;
    }
    const r = routes[current];
    $("#breadcrumb").textContent = "Portal / " + r.name;
    document.title = r.name + " | Portal Clínica-Escola";
    main.innerHTML =
      '<div class="loading" role="status">Carregando registros…</div>';
    try {
      if (current === "inicio") {
        await overview(seq);
        return;
      }
      if (current === "carteirinha") {
        await identity(seq);
        return;
      }
      let qs = "";
      if (["aulas", "atividades"].includes(current) && s.params.get("turma"))
        qs = "?turmaId=" + encodeURIComponent(s.params.get("turma"));
      if (current === "entregas" && s.params.get("atividade"))
        qs = "?atividadeId=" + encodeURIComponent(s.params.get("atividade"));
      const result = await req("get", "/" + current + qs);
      if (seq !== renderSequence) return;
      rows = result;
      const action =
        (r.action && canCreate(current)
          ? `<button class="btn primary" data-action="new"><span aria-hidden="true">+</span> ${esc(r.action)}</button>`
          : "") +
        (current === "boletim" && rows.length
          ? '<button class="btn" data-action="export-grades">Exportar notas CSV</button>'
          : "");
      main.innerHTML =
        heading(r.name, r.desc, action) +
        (s.params.get("turma")
          ? '<p style="margin:-10px 0 20px"><a href="#' +
            current +
            '">← Todas as turmas</a></p>'
          : "") +
        (current === "financeiro"
          ? '<div class="notice warning"><strong>Demonstração financeira.</strong> Nenhuma cobrança, Pix, boleto ou pagamento real é realizado.</div>'
          : current === "boletim"
            ? '<div class="notice"><strong>Acompanhamento individual, sem validade oficial.</strong> Escala de 0 a 10 por avaliação. Não há cálculo automático de média, aprovação ou recuperação.</div>'
            : "") +
        (["aulas", "atividades"].includes(current) ? cards() : shellTable());
      if (!["aulas", "atividades"].includes(current)) {
        if (current === "financeiro") {
          const open = rows
            .filter((f) => f.status === "ABERTO")
            .reduce((s, f) => s + Math.round(Number(f.valor) * 100), 0);
          $(".panel").insertAdjacentHTML(
            "afterbegin",
            `<div class="summary-strip"><span>Em aberto, valor simulado<strong>${money(open / 100)}</strong></span><span>Lançamentos<strong>${rows.length}</strong></span></div>`,
          );
        }
        wireTable();
      }
    } catch (e) {
      if (seq === renderSequence)
        main.innerHTML =
          heading(
            "Não foi possível carregar.",
            e.message || "Verifique sua conexão e tente novamente.",
          ) +
          '<button class="btn primary" data-action="reload">Tentar novamente</button>';
    } finally {
      if (seq === renderSequence) main.setAttribute("aria-busy", "false");
    }
  }
  async function options(route, predicate = () => true, label = (r) => r.nome) {
    return (await req("get", "/" + route))
      .filter(predicate)
      .map((r) => ({ value: r.id, label: label(r) }));
  }
  function field(f) {
    const id = "field-" + f.name;
    let control;
    if (f.type === "select")
      control = `<select id="${id}" name="${f.name}" ${f.required === false ? "" : "required"}><option value="">Selecione…</option>${f.options.map((o) => `<option value="${esc(o.value)}" ${String(o.value) === String(f.value) ? "selected" : ""}>${esc(o.label)}</option>`).join("")}</select>`;
    else if (f.type === "textarea")
      control = `<textarea id="${id}" name="${f.name}" ${f.required === false ? "" : "required"} maxlength="${f.max || 12000}">${esc(f.value || "")}</textarea>`;
    else
      control = `<input id="${id}" name="${f.name}" type="${f.type || "text"}" ${f.required === false ? "" : "required"} ${f.min != null ? 'min="' + esc(f.min) + '"' : ""} ${f.max != null ? (f.type === "number" ? "max" : "maxlength") + '="' + esc(f.max) + '"' : ""} ${f.step ? 'step="' + esc(f.step) + '"' : ""} ${f.minLength ? 'minlength="' + f.minLength + '"' : ""} ${f.type === "password" ? 'autocomplete="new-password"' : 'autocomplete="off"'} value="${esc(f.value ?? "")}">`;
    return `<div class="field ${f.wide ? "wide" : ""}"><label for="${id}">${esc(f.label)}${f.required === false ? ' <span class="muted">(opcional)</span>' : ""}</label>${control}${f.help ? `<small>${esc(f.help)}</small>` : ""}</div>`;
  }
  function showDialog(title, body) {
    returnFocus = document.activeElement;
    $("#dialog-title").textContent = title;
    $("#dialog-body").innerHTML = body;
    dirty = false;
    dialog.showModal();
  }
  function closeDialog(force = false) {
    if (
      dirty &&
      !force &&
      !confirm("Descartar as alterações não salvas deste formulário?")
    )
      return;
    dirty = false;
    dialog.close();
    returnFocus?.focus();
  }
  function form(
    title,
    fields,
    save,
    { help = "", checkbox = "", submit = "Salvar registro" } = {},
  ) {
    showDialog(
      title,
      `${help ? `<p class="dialog-help">${esc(help)}</p>` : ""}<form id="entity-form"><p id="form-error" class="notice error" role="alert" tabindex="-1" hidden></p><div class="form-grid">${fields.map(field).join("")}</div>${checkbox}<div class="form-actions"><button type="button" class="btn" data-close>Cancelar</button><button type="submit" class="btn primary">${esc(submit)}</button></div></form>`,
    );
    const element = $("#entity-form");
    element.addEventListener("input", () => {
      dirty = true;
    });
    element.addEventListener("submit", async (ev) => {
      ev.preventDefault();
      const button = $("[type=submit]", element);
      button.disabled = true;
      button.textContent = "Salvando…";
      const values = Object.fromEntries(new FormData(element));
      const error = $("#form-error");
      error.hidden = true;
      try {
        await save(values);
        closeDialog(true);
        notify("Registro salvo com sucesso.");
        ctx = await req("get", "/contexto");
        await render();
      } catch (e) {
        error.textContent =
          e.message || "Não foi possível salvar. Tente novamente.";
        error.hidden = false;
        Object.keys(e.fieldErrors || {}).forEach((key) =>
          element.elements.namedItem(key)?.setAttribute("aria-invalid", "true"),
        );
        error.focus();
      } finally {
        button.disabled = false;
        button.textContent = submit;
      }
    });
  }
  async function entity(record) {
    const edit = !!record;
    const r = record || {};
    let fields, save;
    switch (current) {
      case "pessoas": {
        const options = (
          is("COORDENACAO") ? ["ALUNO", "PROFESSOR"] : Object.keys(roles)
        ).map((value) => ({ value, label: roles[value] }));
        fields = [
          {
            name: "nome",
            label: "Nome completo",
            max: 120,
            value: r.nome,
            wide: true,
          },
          ...(!edit
            ? [
                {
                  name: "email",
                  label: "E-mail de acesso",
                  type: "email",
                  max: 160,
                  wide: true,
                },
                {
                  name: "senha",
                  label: "Senha inicial",
                  type: "password",
                  minLength: 12,
                  max: 64,
                  help: "De 12 a 64 caracteres. Compartilhe por um canal privado.",
                  wide: true,
                },
              ]
            : []),
          {
            name: "perfil",
            label: "Perfil nesta unidade",
            type: "select",
            options,
            value: r.perfil,
          },
          ...(edit
            ? [
                {
                  name: "ativo",
                  label: "Acesso à conta",
                  type: "select",
                  options: [
                    { value: "true", label: "Ativo" },
                    { value: "false", label: "Suspenso" },
                  ],
                  value: String(r.ativo),
                },
              ]
            : []),
        ];
        save = (v) =>
          req(
            edit ? "patch" : "post",
            "/pessoas" + (edit ? "/" + r.id : ""),
            edit ? { ...v, ativo: v.ativo === "true" } : v,
          );
        break;
      }
      case "cursos":
        fields = [
          {
            name: "codigo",
            label: "Código do curso",
            max: 30,
            value: r.codigo,
          },
          {
            name: "cargaHoraria",
            label: "Carga horária (horas)",
            type: "number",
            min: 1,
            max: 20000,
            value: r.carga_horaria,
          },
          {
            name: "nome",
            label: "Nome do curso",
            max: 160,
            value: r.nome,
            wide: true,
          },
        ];
        save = (v) =>
          req(edit ? "put" : "post", "/cursos" + (edit ? "/" + r.id : ""), {
            ...v,
            cargaHoraria: Number(v.cargaHoraria),
          });
        break;
      case "disciplinas":
        fields = [
          {
            name: "cursoId",
            label: "Curso",
            type: "select",
            options: await options("cursos", (c) => c.ativo),
            value: r.curso_id,
            wide: true,
          },
          {
            name: "codigo",
            label: "Código da disciplina",
            max: 30,
            value: r.codigo,
          },
          {
            name: "cargaHoraria",
            label: "Carga horária (horas)",
            type: "number",
            min: 1,
            max: 20000,
            value: r.carga_horaria,
          },
          {
            name: "nome",
            label: "Nome da disciplina",
            max: 160,
            value: r.nome,
            wide: true,
          },
        ];
        save = (v) =>
          req(
            edit ? "put" : "post",
            "/disciplinas" + (edit ? "/" + r.id : ""),
            {
              ...v,
              cursoId: Number(v.cursoId),
              cargaHoraria: Number(v.cargaHoraria),
            },
          );
        break;
      case "turmas": {
        const [disc, prof] = await Promise.all([
          options(
            "disciplinas",
            () => true,
            (d) => d.nome + " · " + d.curso_nome,
          ),
          options("pessoas", (p) => p.perfil === "PROFESSOR" && p.ativo),
        ]);
        fields = [
          {
            name: "disciplinaId",
            label: "Disciplina",
            type: "select",
            options: disc,
            value: r.disciplina_id,
            wide: true,
          },
          {
            name: "professorId",
            label: "Professor responsável",
            type: "select",
            options: prof,
            value: r.professor_id,
            wide: true,
          },
          {
            name: "nome",
            label: "Nome da turma",
            max: 160,
            value: r.nome,
            wide: true,
          },
          {
            name: "periodo",
            label: "Período letivo",
            max: 30,
            value: r.periodo,
            help: "Exemplo: 2026.2",
          },
          {
            name: "vagas",
            label: "Vagas",
            type: "number",
            min: 1,
            max: 1000,
            value: r.vagas ?? 40,
          },
        ];
        save = (v) =>
          req(edit ? "put" : "post", "/turmas" + (edit ? "/" + r.id : ""), {
            ...v,
            disciplinaId: Number(v.disciplinaId),
            professorId: Number(v.professorId),
            vagas: Number(v.vagas),
          });
        break;
      }
      case "matriculas": {
        const [turmas, pessoas] = await Promise.all([
          options(
            "turmas",
            () => true,
            (t) => t.nome + " · " + t.periodo,
          ),
          options("pessoas", (p) => p.perfil === "ALUNO" && p.ativo),
        ]);
        fields = [
          {
            name: "turmaId",
            label: "Turma",
            type: "select",
            options: turmas,
            wide: true,
          },
          {
            name: "alunoId",
            label: "Aluno",
            type: "select",
            options: pessoas,
            wide: true,
          },
        ];
        save = (v) =>
          req("post", "/matriculas", {
            turmaId: Number(v.turmaId),
            alunoId: Number(v.alunoId),
          });
        break;
      }
      case "aulas":
      case "atividades": {
        const atividade = current === "atividades";
        const turmas = await options("turmas");
        fields = [
          {
            name: "turmaId",
            label: "Turma",
            type: "select",
            options: edit
              ? turmas.filter((t) => Number(t.value) === Number(r.turma_id))
              : turmas,
            value: r.turma_id || state().params.get("turma"),
            wide: true,
          },
          {
            name: "titulo",
            label: "Título",
            max: 160,
            value: r.titulo,
            wide: true,
          },
          ...(!atividade
            ? [
                {
                  name: "conteudo",
                  label: "Conteúdo da aula",
                  type: "textarea",
                  value: r.conteudo,
                  wide: true,
                },
                {
                  name: "materialUrl",
                  label: "Link HTTPS do material",
                  type: "url",
                  required: false,
                  max: 2000,
                  value: r.material_url,
                  wide: true,
                  help: "O link abre em nova aba. O portal não hospeda arquivos ou vídeos.",
                },
              ]
            : [
                {
                  name: "instrucoes",
                  label: "Enunciado e orientações",
                  type: "textarea",
                  value: r.instrucoes,
                  wide: true,
                },
                {
                  name: "prazo",
                  label: "Prazo de entrega (seu horário local)",
                  type: "datetime-local",
                  value: r.prazo ? localDateTime(r.prazo) : "",
                  wide: true,
                },
              ]),
        ];
        save = (v) =>
          req(edit ? "put" : "post", "/" + current + (edit ? "/" + r.id : ""), {
            ...v,
            turmaId: Number(v.turmaId),
            ...(atividade ? { prazo: new Date(v.prazo).toISOString() } : {}),
          });
        break;
      }
      case "financeiro":
        fields = [
          {
            name: "alunoId",
            label: "Aluno",
            type: "select",
            options: await options(
              "pessoas",
              (p) => p.perfil === "ALUNO" && p.ativo,
            ),
            wide: true,
          },
          {
            name: "descricao",
            label: "Descrição do lançamento demonstrativo",
            max: 240,
            wide: true,
          },
          {
            name: "valor",
            label: "Valor simulado (R$)",
            type: "number",
            min: 0.01,
            max: 9999999999.99,
            step: ".01",
          },
          { name: "vencimento", label: "Vencimento", type: "date" },
        ];
        save = (v) =>
          req("post", "/financeiro", {
            ...v,
            alunoId: Number(v.alunoId),
            valor: Number(v.valor),
          });
        break;
      default:
        return;
    }
    form(
      edit
        ? "Editar " +
            ({
              pessoas: "acesso",
              cursos: "curso",
              disciplinas: "disciplina",
              turmas: "turma",
              aulas: "aula",
              atividades: "atividade",
            }[current] || "registro")
        : routes[current].action,
      fields,
      save,
      {
        help:
          current === "matriculas"
            ? "O aluno precisa estar ativo e a turma precisa ter vaga. Matrículas canceladas continuam no histórico."
            : current === "financeiro"
              ? "Não gera boleto, cobrança ou pagamento real."
              : current === "atividades"
                ? "A atividade será publicada imediatamente. Após receber entregas, o enunciado e o prazo ficam protegidos."
                : "",
      },
    );
  }
  function localDateTime(value) {
    const d = new Date(value);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}T${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
  }
  function grade(r) {
    form(
      "Avaliar entrega",
      [
        {
          name: "nota",
          label: "Nota individual (0 a 10)",
          type: "number",
          min: 0,
          max: 10,
          step: ".01",
          value: r.nota,
        },
        {
          name: "feedback",
          label: "Feedback para o aluno",
          type: "textarea",
          max: 4000,
          required: false,
          value: r.feedback,
          wide: true,
        },
      ],
      (v) =>
        req("put", "/entregas/" + r.id + "/avaliacao", {
          nota: Number(v.nota),
          feedback: v.feedback,
          publicada: v.publicada === "on",
        }),
      {
        help: `${r.aluno_nome} · ${r.atividade_titulo}. O aluno só vê a nota depois da publicação.`,
        checkbox: `<label class="checkbox-field"><input type="checkbox" name="publicada" ${r.publicada ? "checked" : ""}><span>Publicar avaliação para o aluno. Desmarque para manter como rascunho.</span></label>`,
        submit: "Salvar avaliação",
      },
    );
  }
  function delivery(r) {
    form(
      "Enviar resposta",
      [
        {
          name: "conteudo",
          label: "Sua resposta final",
          type: "textarea",
          max: 12000,
          wide: true,
        },
      ],
      (v) =>
        req("post", "/entregas", { atividadeId: r.id, conteudo: v.conteudo }),
      {
        help:
          r.titulo +
          " · prazo " +
          shortDate(r.prazo, true) +
          ". O envio é final: não poderá ser substituído neste portal.",
        checkbox:
          '<label class="checkbox-field"><input type="checkbox" name="confirmacao" required><span>Conferi minha resposta e confirmo o envio final.</span></label>',
        submit: "Confirmar envio final",
      },
    );
  }
  function exportGrades() {
    const csvEscape = (value) => {
      let s = String(value ?? "");
      if (/^[=+@\-\t\r]/.test(s)) s = "'" + s;
      return '"' + s.replaceAll('"', '""') + '"';
    };
    const lines = [
      ["DOCUMENTO DEMONSTRATIVO - SEM VALIDADE OFICIAL"],
      ["Disciplina", "Turma", "Aluno", "Atividade", "Nota 0-10", "Feedback"],
      ...rows.map((r) => [
        r.disciplina_nome,
        r.turma_nome,
        r.aluno_nome,
        r.atividade_titulo,
        r.nota,
        r.feedback,
      ]),
    ];
    const blob = new Blob(
      [
        "\uFEFF" +
          lines.map((row) => row.map(csvEscape).join(";")).join("\r\n"),
      ],
      { type: "text/csv;charset=utf-8" },
    );
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "notas-demonstrativas.csv";
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  $("#conteudo").addEventListener("click", async (event) => {
    const target = event.target.closest("[data-action],[data-page]");
    if (!target) return;
    if (target.hasAttribute("data-page")) {
      page = Number(target.dataset.page);
      syncSearch();
      drawTable();
      return;
    }
    const action = target.dataset.action;
    const id = Number(target.dataset.id);
    const record = rows.find((r) => Number(r.id) === id);
    target.disabled = true;
    try {
      switch (action) {
        case "new":
          await entity(null);
          break;
        case "edit":
          await entity(record);
          break;
        case "reload":
          await render();
          break;
        case "open-turma":
          go("aulas?turma=" + id);
          break;
        case "open-deliveries":
          go("entregas?atividade=" + id);
          break;
        case "submit-delivery":
          delivery(record);
          break;
        case "grade":
          grade(record);
          break;
        case "read-delivery":
          showDialog(
            "Resposta enviada",
            `<p class="dialog-help">${esc(record.aluno_nome)} · ${esc(record.atividade_titulo)} · ${esc(shortDate(record.enviado_em, true))}</p><div class="prose">${esc(record.conteudo)}</div>${record.feedback ? `<hr><h3>Feedback publicado</h3><p class="prose">${esc(record.feedback)}</p>` : ""}<div class="form-actions"><button class="btn" data-close>Fechar</button></div>`,
          );
          break;
        case "cancel-enrolment":
          if (
            confirm(
              "Cancelar esta matrícula? O aluno perderá acesso à turma, mas o histórico será preservado.",
            )
          ) {
            await req("post", "/matriculas/" + id + "/cancelar", {});
            notify("Matrícula cancelada; histórico preservado.");
            ctx = await req("get", "/contexto");
            await render();
          }
          break;
        case "settle-demo":
          if (
            confirm(
              "Registrar somente uma baixa demonstrativa? Nenhum pagamento real será processado.",
            )
          ) {
            await req("post", "/financeiro/" + id + "/baixa-demonstrativa", {});
            notify(
              "Baixa demonstrativa registrada. Nenhum pagamento realizado.",
            );
            await render();
          }
          break;
        case "print":
          window.print();
          break;
        case "export-grades":
          exportGrades();
          break;
      }
    } catch (e) {
      notify(e.message || "Não foi possível concluir a operação.", true);
    } finally {
      target.disabled = false;
    }
  });
  $("#dialog-close").addEventListener("click", () => closeDialog());
  $("#dialog-body").addEventListener("click", (e) => {
    if (e.target.closest("[data-close]")) closeDialog();
  });
  dialog.addEventListener("cancel", (e) => {
    e.preventDefault();
    closeDialog();
  });
  $("#menu-toggle").addEventListener("click", () =>
    menu(!$("#sidebar").classList.contains("open")),
  );
  $("#nav-overlay").addEventListener("click", () => menu(false));
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !dialog.open) menu(false);
  });
  async function logout(changeUnit = false) {
    if (
      !confirm(
        changeUnit
          ? "Encerrar a sessão atual para entrar em outra unidade?"
          : "Sair da sua conta?",
      )
    )
      return;
    try {
      await Api.post("/api/auth/logout");
      location.replace("/index.html?saiu=1");
    } catch (e) {
      notify(
        e.message || "Não foi possível encerrar a sessão. Tente novamente.",
        true,
      );
    }
  }
  $("#logout").addEventListener("click", () => logout());
  $("#trocar-unidade").addEventListener("click", (e) => {
    e.preventDefault();
    logout(true);
  });
  $(".skip").addEventListener("click", (event) => {
    event.preventDefault();
    $("#conteudo").focus();
  });
  matchMedia("(max-width:780px)").addEventListener("change", () => menu(false));
  window.addEventListener("beforeunload", (event) => {
    if (dirty) {
      event.preventDefault();
      event.returnValue = "";
    }
  });
  window.addEventListener("hashchange", () => render());
  (async () => {
    try {
      ctx = await req("get", "/contexto");
      window.PortalExperience.init(ctx, routes, icon);
      $("#campus-name").textContent = ctx.unidade.nome;
      $("#user-name").textContent = ctx.usuario.nome;
      $("#user-role").textContent =
        roles[ctx.usuario.perfil] || ctx.usuario.perfil;
      $("#avatar").textContent = ctx.usuario.nome
        .split(/\s+/)
        .slice(0, 2)
        .map((s) => s[0])
        .join("")
        .toUpperCase();
      await render();
    } catch (e) {
      $("#conteudo").innerHTML =
        heading(
          "Portal indisponível.",
          e.message || "Entre novamente para continuar.",
        ) + '<a class="btn primary" href="/index.html">Voltar ao login</a>';
    }
  })();
})();
