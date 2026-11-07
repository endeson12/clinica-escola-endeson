/* global Form */
(() => {
  "use strict";
  const $ = (q) => document.querySelector(q);
  const esc = Form.esc;
  const source = "https://conheca.sereducacional.com/uninassauday";
  let modules = [],
    glyph = () => "",
    slide = 0,
    opener = null;
  const reduced = matchMedia("(prefers-reduced-motion: reduce)");
  let paused = false;
  try {
    paused = localStorage.getItem("portal-motion-paused") === "true";
  } catch {
    /* Storage may be disabled. */
  }
  function motion() {
    document.body.classList.toggle("motion-off", paused || reduced.matches);
    const button = $("#motion-toggle");
    if (!button) return;
    button.disabled = reduced.matches;
    button.textContent = reduced.matches
      ? "Movimento reduzido pelo sistema"
      : paused
        ? "Ativar animações"
        : "Pausar animações";
    button.setAttribute("aria-pressed", String(paused || reduced.matches));
  }
  motion();
  reduced.addEventListener("change", motion);
  $("#motion-toggle")?.addEventListener("click", () => {
    paused = !paused;
    try {
      localStorage.setItem("portal-motion-paused", String(paused));
    } catch {
      /* Nonessential preference only. */
    }
    motion();
  });
  const details = {
    cursos: "Organize a oferta acadêmica",
    pessoas: "Cadastros e permissões",
    turmas: "Acesse suas turmas",
    matriculas: "Acompanhe os vínculos",
    aulas: "Conteúdo para o próximo passo",
    atividades: "Enunciados, prazos e respostas",
    entregas: "Acompanhe as avaliações",
    boletim: "Consulte notas publicadas",
    financeiro: "Extrato demonstrativo",
    carteirinha: "Identificação demonstrativa",
    auditoria: "Consulte o histórico de ações",
  };
  function preferred(ctx) {
    const picks =
      {
        ADMIN: ["cursos", "pessoas", "turmas", "matriculas"],
        COORDENACAO: ["turmas", "pessoas", "cursos", "matriculas"],
        PROFESSOR: ["turmas", "aulas", "atividades", "entregas"],
        ALUNO: ["aulas", "atividades", "boletim", "carteirinha"],
        FINANCEIRO: ["financeiro", "pessoas"],
        RECEPCAO: [],
      }[ctx.usuario.perfil] || [];
    return picks
      .map((key) => modules.find((m) => m.key === key))
      .filter(Boolean);
  }
  const gallery = [
    {
      image: "campus-conceitual.webp",
      title: "Um olhar para o futuro do cuidado.",
      description:
        "Ensino e saúde se encontram nesta arte conceitual criada para o portal. Não é uma fotografia de uma unidade real.",
      tag: "ARTE CONCEITUAL · IA",
      alt: "Ilustração conceitual de estudantes de saúde em um átrio universitário azul e dourado.",
    },
    {
      image: "estudo-saude.webp",
      title: "Conhecimento que vira prática.",
      description:
        "Caderno, escuta e atenção aos detalhes: uma composição original sobre a formação em saúde. Imagem gerada por IA.",
      tag: "ARTE ORIGINAL · IA",
      alt: "Composição conceitual de um caderno azul, lápis amarelo e estetoscópio sobre uma mesa.",
    },
    {
      image: "uninassau-campanha.webp",
      title: "Uma referência da comunidade acadêmica.",
      description:
        "Imagem da campanha pública UNINASSAU Day, do Grupo Ser Educacional. Referência visual; não anuncia evento atual nem representa pessoa cadastrada neste portal.",
      tag: "CAMPANHA PÚBLICA · UNINASSAU",
      alt: "Modelo com livros em imagem da campanha pública UNINASSAU Day.",
      campaign: true,
    },
  ];
  function paintGallery() {
    const stage = $("#gallery-stage"),
      caption = $("#gallery-caption");
    if (!stage || !caption) return;
    const item = gallery[slide];
    stage.classList.toggle("campaign", !!item.campaign);
    stage.innerHTML = `<img src="/img/${item.image}" alt="${esc(item.alt)}" width="${item.campaign ? 316 : 1536}" height="${item.campaign ? 485 : 1024}" loading="lazy" decoding="async"/>${item.campaign ? '<img class="campaign-mark" src="/img/uninassau-referencia.webp" width="281" height="59" alt="UNINASSAU — marca da fonte de referência"/><span class="campaign-label">Aprender.<br/>Conectar.<br/>Transformar.</span>' : ""}<span class="gallery-tag">${item.tag}</span>`;
    caption.innerHTML = `<h3>${item.title}</h3><p>${item.description}</p>${item.campaign ? `<a href="${source}" target="_blank" rel="noopener noreferrer">Ver a fonte pública ↗</a>` : ""}`;
    document
      .querySelectorAll("[data-gallery-slide]")
      .forEach((b) =>
        b.setAttribute(
          "aria-pressed",
          String(Number(b.dataset.gallerySlide) === slide),
        ),
      );
    $("#gallery-status").textContent =
      `Imagem ${slide + 1} de ${gallery.length}: ${item.title}`;
  }
  function drawModules() {
    const term = $("#module-search")
      .value.normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLocaleLowerCase("pt-BR")
      .trim();
    const filtered = modules.filter((m) =>
      m.name
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLocaleLowerCase("pt-BR")
        .includes(term),
    );
    $("#module-results").innerHTML = filtered
      .map(
        (m) =>
          `<a class="module-result" href="#${m.key}">${glyph(m.icon)}<strong>${esc(m.name)}</strong><span>Abrir ↗</span></a>`,
      )
      .join("");
    $("#module-empty").hidden = !!filtered.length;
  }
  function openModules(button) {
    const modal = $("#module-dialog");
    if (!modules.length || !modal || $("#entity-dialog")?.open) return;
    opener = button || $("[data-open-modules]");
    $("#module-search").value = "";
    drawModules();
    modal.showModal();
    $("#module-search").focus();
  }
  $("#module-close")?.addEventListener("click", () =>
    $("#module-dialog").close(),
  );
  $("#module-dialog")?.addEventListener("close", () => opener?.focus());
  $("#module-search")?.addEventListener("input", drawModules);
  $("#module-search")?.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      e.preventDefault();
      $("#module-dialog").close();
      return;
    }
    if ((e.key === "ArrowDown" || e.key === "Enter") && $(".module-result")) {
      e.preventDefault();
      if (e.key === "Enter") $(".module-result").click();
      else $(".module-result").focus();
    }
  });
  $("#module-results")?.addEventListener("click", (e) => {
    if (e.target.closest("a")) $("#module-dialog").close();
  });
  document.addEventListener("keydown", (e) => {
    if (
      (e.ctrlKey || e.metaKey) &&
      e.key.toLowerCase() === "k" &&
      modules.length
    ) {
      e.preventDefault();
      if (!$("#module-dialog").open) openModules();
    }
  });
  document.addEventListener("click", (e) => {
    const trigger = e.target.closest("[data-open-modules]");
    if (trigger) openModules(trigger);
    if (e.target.closest("[data-scroll-shortcuts]")) {
      $("#atalhos")?.scrollIntoView({
        behavior: paused || reduced.matches ? "instant" : "smooth",
        block: "center",
      });
      $("#atalhos")?.focus({ preventScroll: true });
    }
    const dot = e.target.closest("[data-gallery-slide]");
    const arrow = e.target.closest("[data-gallery-direction]");
    if (dot) {
      slide = Number(dot.dataset.gallerySlide);
      paintGallery();
    }
    if (arrow) {
      slide =
        (slide + Number(arrow.dataset.galleryDirection) + gallery.length) %
        gallery.length;
      paintGallery();
    }
  });
  window.PortalExperience = {
    init(ctx, routes, icon) {
      glyph = icon;
      modules = Object.entries(routes)
        .filter(([, r]) => r.roles.includes(ctx.usuario.perfil))
        .map(([key, r]) => ({ key, ...r }));
    },
    hero(ctx) {
      const lines = {
        ALUNO: [
          "Seu próximo passo<br/>começa <em>aqui.</em>",
          "Aulas, atividades e tudo o que acompanha a sua jornada de aprendizado.",
        ],
        PROFESSOR: [
          "Ensinar é abrir<br/><em>novos caminhos.</em>",
          "Organize o conteúdo, acompanhe as entregas e dê espaço ao desenvolvimento.",
        ],
        RECEPCAO: [
          "Cada encontro<br/>começa com <em>cuidado.</em>",
          "Acesse a rotina clínica com segurança e atenção a cada atendimento.",
        ],
        FINANCEIRO: [
          "Clareza para<br/>acompanhar <em>cada etapa.</em>",
          "Organize os lançamentos exclusivamente demonstrativos da sua unidade.",
        ],
      }[ctx.usuario.perfil] || [
        "Aprender conecta.<br/><em>Cuidar transforma.</em>",
        "Pessoas, ensino e gestão em um espaço feito para acompanhar cada etapa.",
      ];
      return `<section class="portal-hero" aria-label="Boas-vindas ao portal"><img src="/img/campus-conceitual.webp" srcset="/img/campus-conceitual-640.webp 640w, /img/campus-conceitual.webp 1536w" sizes="(max-width:780px) 100vw, calc(100vw - 330px)" width="1536" height="1024" alt="" fetchpriority="high"/><div class="hero-copy"><span class="hero-eyebrow">ENSINO + CUIDADO</span><h2>${lines[0]}</h2><p>${lines[1]}</p><button type="button" class="btn gold" data-scroll-shortcuts>Explorar meus atalhos <span aria-hidden="true">↗</span></button></div><span class="hero-line">SEU PORTAL, MAIS PERTO.</span><small class="hero-caption">Arte conceitual gerada por IA · não é campus real</small></section>`;
    },
    shortcuts(ctx) {
      const links = preferred(ctx);
      return `<section aria-labelledby="atalhos"><div class="shortcuts-heading"><div><h2 id="atalhos" tabindex="-1">Seu dia, mais simples.</h2><p>Atalhos disponíveis para o seu perfil</p></div><button type="button" class="btn small" data-open-modules>Todos os módulos ↗</button></div><div class="quick-grid">${links.map((m) => `<a class="quick-link" href="#${m.key}"><span class="quick-icon">${glyph(m.icon)}</span><span class="quick-arrow" aria-hidden="true">↗</span><strong>${esc(m.name)}</strong><small>${esc(details[m.key] || "Acessar módulo")}</small></a>`).join("")}${ctx.clinicaPermitida && !links.length ? `<a class="quick-link" href="/dashboard.html"><span class="quick-icon">${glyph("shield")}</span><strong>Gestão clínica</strong><small>Pacientes e atendimentos</small></a>` : ""}</div></section>`;
    },
    discovery(ctx) {
      const target =
        modules.find((m) => m.key === "aulas") ||
        modules.find((m) => m.key === "financeiro") ||
        modules[0];
      return `<section class="discovery" aria-label="Inspirações e referências visuais"><article class="gallery" aria-label="Galeria de imagens do portal"><div class="gallery-stage" id="gallery-stage"></div><div class="gallery-caption" id="gallery-caption"></div><div class="gallery-controls"><div class="gallery-dots">${gallery.map((item, i) => `<button type="button" data-gallery-slide="${i}" aria-label="Ver imagem ${i + 1}" aria-pressed="${i === slide}"></button>`).join("")}</div><div class="gallery-arrows"><button type="button" data-gallery-direction="-1" aria-label="Imagem anterior">←</button><button type="button" data-gallery-direction="1" aria-label="Próxima imagem">→</button></div></div><p class="sr-only" id="gallery-status" role="status" aria-live="polite"></p><small class="gallery-credit">Artes de IA + referência pública UNINASSAU Day. Portal independente, sem vínculo ou integração institucional oficial.</small></article><article class="study-panel"><img src="/img/estudo-saude-640.webp" width="640" height="427" alt="Arte conceitual de materiais de estudo e saúde." loading="lazy" decoding="async"/><div class="study-copy"><p class="eyebrow">APRENDER É UM MOVIMENTO</p><h2>Um passo de cada vez.<br/>Todos os dias.</h2><p>Encontre o que precisa para continuar: ${ctx.usuario.perfil === "RECEPCAO" ? "organização e atenção à rotina clínica." : "conteúdo, acompanhamento e organização da sua rotina."}</p><a class="btn" href="#${target.key}">Abrir ${esc(target.name.toLocaleLowerCase("pt-BR"))} ↗</a><small>Ilustração original gerada por IA.</small></div></article></section>`;
    },
    mountOverview() {
      paintGallery();
    },
  };
  const tabs = [...document.querySelectorAll("[data-story-tab]")];
  const stories = {
    aprender: [
      "Conhecimento que acompanha você.",
      "Encontre aulas, materiais e atividades publicados para suas turmas.",
    ],
    acompanhar: [
      "Cada etapa, no seu tempo.",
      "Consulte entregas e notas publicadas, com acesso individual por perfil.",
    ],
    organizar: [
      "Uma rotina com mais clareza.",
      "Pessoas, turmas e atendimento clínico organizados por unidade e permissão.",
    ],
  };
  function story(tab) {
    const copy = stories[tab.dataset.storyTab];
    tabs.forEach((t) => {
      const active = t === tab;
      t.setAttribute("aria-selected", String(active));
      t.tabIndex = active ? 0 : -1;
    });
    $("#story-panel").setAttribute("aria-labelledby", tab.id);
    $("#story-panel h2").textContent = copy[0];
    $("#story-panel p").textContent = copy[1];
  }
  tabs.forEach((tab, i) => {
    tab.addEventListener("click", () => story(tab));
    tab.addEventListener("keydown", (e) => {
      const next =
        e.key === "ArrowRight"
          ? (i + 1) % tabs.length
          : e.key === "ArrowLeft"
            ? (i + tabs.length - 1) % tabs.length
            : e.key === "Home"
              ? 0
              : e.key === "End"
                ? tabs.length - 1
                : null;
      if (next !== null) {
        e.preventDefault();
        story(tabs[next]);
        tabs[next].focus();
      }
    });
  });
})();
