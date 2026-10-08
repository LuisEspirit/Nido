/* =====================================================================
   Nido - utilidades comunes del frontend
   Sesion JWT, llamadas a la API REST, menu por rol, iconos, toasts,
   modales de confirmacion, formato de fechas/moneda y formularios.
   ===================================================================== */
const Nido = (() => {
  const API = "/api/v1";
  const CLAVE_TOKEN = "nido.token";
  const CLAVE_USUARIO = "nido.usuario";

  // ---------- Iconos (trazo 2 px, rejilla 24 - estilo Lucide) ----------
  const ICONOS = {
    home: '<path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5"/>',
    building: '<rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4M8 6h.01M12 6h.01M16 6h.01M8 10h.01M12 10h.01M16 10h.01M8 14h.01M12 14h.01M16 14h.01"/>',
    bed: '<path d="M2 4v16M2 8h18a2 2 0 0 1 2 2v10M2 17h20M6 8v9"/>',
    calendar: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
    users: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1"/>',
    dollar: '<path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>',
    chart: '<path d="M3 3v18h18"/><path d="M8 17V9M13 17V5M18 17v-6"/>',
    alert: '<path d="m21.73 18-8-14a2 2 0 0 0-3.46 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4M12 17h.01"/>',
    wrench: '<path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94z"/>',
    clipboard: '<rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="m9 14 2 2 4-4"/>',
    camera: '<path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3z"/><circle cx="12" cy="13" r="3"/>',
    map: '<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/>',
    bell: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
    search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
    filter: '<path d="M22 3H2l8 9.46V19l4 2v-8.54z"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>',
    login: '<path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M15 12H3"/>',
    check: '<path d="M20 6 9 17l-5-5"/>',
    x: '<path d="M18 6 6 18M6 6l12 12"/>',
    shield: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/>',
    key: '<circle cx="7.5" cy="15.5" r="5.5"/><path d="m21 2-9.6 9.6M15.5 7.5l3 3L22 7l-3-3"/>',
    clock: '<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
    globe: '<circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>',
    edit: '<path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/>',
    trash: '<path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>',
    eye: '<path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>',
    right: '<path d="m9 18 6-6-6-6"/>',
    left: '<path d="m15 18-6-6 6-6"/>',
    info: '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>',
    error: '<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/>',
    ok: '<circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>',
    list: '<path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/>',
    sparkle: '<path d="M12 3l1.9 5.8L20 11l-6.1 2.2L12 19l-1.9-5.8L4 11l6.1-2.2z"/>',
    sliders: '<path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6"/>',
    upload: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>',
    image: '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.1-3.1a2 2 0 0 0-2.8 0L6 21"/>',
    history: '<path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5M12 7v5l4 2"/>'
  };
  const icono = (nombre, clase = "") =>
    `<svg class="ico ${clase}" viewBox="0 0 24 24" aria-hidden="true">${ICONOS[nombre] || ""}</svg>`;

  // ---------- Sesion ----------
  const token = () => localStorage.getItem(CLAVE_TOKEN);
  const usuario = () => JSON.parse(localStorage.getItem(CLAVE_USUARIO) || "null");
  const tieneRol = (rol) => (usuario()?.roles || []).includes(rol);
  const esSoloPersonal = () => tieneRol("PERSONAL") && !tieneRol("PROPIETARIO") && !tieneRol("ADMIN");
  function guardarSesion(t, u) { localStorage.setItem(CLAVE_TOKEN, t); localStorage.setItem(CLAVE_USUARIO, JSON.stringify(u)); }
  function cerrarSesion() { localStorage.removeItem(CLAVE_TOKEN); localStorage.removeItem(CLAVE_USUARIO); location.href = "/login.html"; }

  // ---------- API REST ----------
  async function api(metodo, ruta, cuerpo, opciones = {}) {
    const headers = {};
    if (token()) headers.Authorization = "Bearer " + token();
    let body;
    if (cuerpo instanceof FormData) body = cuerpo;
    else if (cuerpo !== undefined) { headers["Content-Type"] = "application/json"; body = JSON.stringify(cuerpo); }
    const r = await fetch(API + ruta, { method: metodo, headers, body });
    if (r.status === 401 && !opciones.sinRedireccion) { cerrarSesion(); throw { status: 401, mensaje: "Tu sesión expiró. Vuelve a iniciar sesión." }; }
    if (r.status === 204) return null;
    const tipo = r.headers.get("content-type") || "";
    const datos = tipo.includes("json") ? await r.json() : null;
    if (!r.ok) throw { status: r.status, mensaje: datos?.mensaje || datos?.error || "No pudimos completar la operación.", campos: datos?.campos };
    return datos;
  }
  const get = (ruta) => api("GET", ruta);
  const post = (ruta, cuerpo) => api("POST", ruta, cuerpo);
  const put = (ruta, cuerpo) => api("PUT", ruta, cuerpo);
  const patch = (ruta, cuerpo) => api("PATCH", ruta, cuerpo);
  const del = (ruta) => api("DELETE", ruta);

  // ---------- Formato ----------
  const moneda = (v, m = "PEN") => v == null ? "—" : (m === "USD" ? "US$ " : "S/ ") + Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const fecha = (v) => v ? new Date(v).toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" }) : "—";
  const MESES = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic"];
  const hora = (v) => v ? String(v).slice(11, 16) : "—";
  const fechaCorta = (v) => v ? `${String(v).slice(8, 10)} ${MESES[+String(v).slice(5, 7) - 1]}` : "—";
  const fechaHora = (v) => v ? `<span style="white-space:nowrap">${fechaCorta(v)} · ${hora(v)}</span>` : "—";
  const esc = (s) => String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const hoyISO = () => { const d = new Date(); d.setMinutes(d.getMinutes() - d.getTimezoneOffset()); return d.toISOString().slice(0, 10); };
  const titulo = (s) => String(s ?? "").toLowerCase().replace(/_/g, " ").replace(/^\w/, c => c.toUpperCase());

  const CLASE_ESTADO = {
    CONFIRMADA: "b-exito", PAGADO: "b-exito", COMPLETADO: "b-exito", RESUELTA: "b-exito", ACTIVO: "b-exito", DISPONIBLE: "b-exito", ACEPTADO: "b-exito", INGRESO: "b-exito",
    PENDIENTE: "b-advertencia", ASIGNADO: "b-advertencia", EN_PROCESO: "b-info", EN_CURSO: "b-info", MEDIA: "b-advertencia",
    CANCELADA: "b-error", CANCELADO: "b-error", ANULADO: "b-error", RECHAZADO: "b-error", ABIERTA: "b-error", INACTIVO: "b-neutro", CRITICA: "b-error", ALTA: "b-error", GASTO: "b-error",
    FINALIZADA: "b-neutro", CERRADA: "b-neutro", BAJA: "b-neutro"
  };
  const badge = (estado) => `<span class="badge ${CLASE_ESTADO[estado] || "b-primario"}">${esc(titulo(estado || "Sin estado"))}</span>`;

  // ---------- Toasts (4 s) ----------
  function toast(tipo, tituloTxt, texto = "") {
    let cont = document.querySelector(".toasts");
    if (!cont) { cont = document.createElement("div"); cont.className = "toasts"; cont.setAttribute("aria-live", "polite"); document.body.appendChild(cont); }
    const ico = { exito: "ok", error: "error", info: "info", advertencia: "alert" }[tipo];
    const el = document.createElement("div");
    el.className = `alerta a-${tipo} toast`; el.setAttribute("role", tipo === "error" ? "alert" : "status");
    el.innerHTML = `${icono(ico)}<div><strong>${esc(tituloTxt)}</strong>${esc(texto)}</div>`;
    cont.appendChild(el); setTimeout(() => el.remove(), 4000);
  }

  // ---------- Modales ----------
  function abrirModal(id) { const m = document.getElementById(id); m.classList.add("abierto"); m.querySelector("input,select,textarea,button")?.focus(); }
  function cerrarModal(id) { document.getElementById(id).classList.remove("abierto"); }
  function confirmar(tituloTxt, mensaje, textoBoton = "Eliminar") {
    return new Promise(resolver => {
      let m = document.getElementById("modal-confirmar");
      if (!m) {
        m = document.createElement("div"); m.id = "modal-confirmar"; m.className = "modal-fondo";
        m.innerHTML = `<div class="modal" role="dialog" aria-modal="true" aria-labelledby="mc-t" style="max-width:460px">
          <div class="modal-cab"><h3 id="mc-t"></h3></div><div class="modal-cuerpo" id="mc-m"></div>
          <div class="modal-pie"><button class="btn btn-secundario" data-r="0">Cancelar</button><button class="btn btn-peligro" data-r="1" id="mc-b"></button></div></div>`;
        document.body.appendChild(m);
      }
      m.querySelector("#mc-t").textContent = tituloTxt; m.querySelector("#mc-m").innerHTML = mensaje; m.querySelector("#mc-b").textContent = textoBoton;
      m.classList.add("abierto");
      m.querySelectorAll("[data-r]").forEach(b => b.onclick = () => { m.classList.remove("abierto"); resolver(b.dataset.r === "1"); });
    });
  }
  document.addEventListener("keydown", e => { if (e.key === "Escape") document.querySelectorAll(".modal-fondo.abierto").forEach(m => m.classList.remove("abierto")); });

  // ---------- Formularios: los "name" de los campos son los nombres del JSON ----------
  function validarCampo(el) {
    const campo = el.closest(".campo"); if (!campo) return true;
    const ok = el.checkValidity();
    campo.classList.toggle("con-error", !ok);
    const msg = campo.querySelector(".error-campo"); if (msg && !ok) msg.textContent = el.dataset.error || el.validationMessage;
    return ok;
  }
  function prepararFormulario(form) {
    form.querySelectorAll(".input").forEach(el => el.addEventListener("blur", () => validarCampo(el)));
  }
  function formularioValido(form) {
    let ok = true; form.querySelectorAll(".input").forEach(el => { if (!validarCampo(el)) ok = false; }); return ok;
  }
  function mostrarErroresApi(form, err) {
    if (err.campos) Object.entries(err.campos).forEach(([nombre, msg]) => {
      const el = form.querySelector(`[name="${nombre}"]`) || form.querySelector(`[name^="${nombre}."]`);
      const campo = el?.closest(".campo"); if (campo) { campo.classList.add("con-error"); campo.querySelector(".error-campo").textContent = msg; }
    });
    toast("error", "Revisa el formulario", err.mensaje);
  }

  // ---------- Layout: barra lateral segun las opciones del rol y barra superior ----------
  const PAGINAS = {
    "/panel": ["panel.html", "home"], "/alojamientos": ["alojamientos.html", "building"], "/reservas": ["reservas.html", "calendar"],
    "/huespedes": ["huespedes.html", "users"], "/servicios": ["servicios.html", "clipboard"], "/incidencias": ["incidencias.html", "alert"],
    "/pagos": ["pagos.html", "dollar"], "/reportes": ["reportes.html", "chart"], "/personal": ["personal.html", "wrench"],
    "/usuarios": ["usuarios.html", "shield"], "/auditoria": ["auditoria.html", "history"], "/mis-tareas": ["tareas.html", "list"]
  };
  const ORDEN = ["/panel", "/alojamientos", "/reservas", "/huespedes", "/servicios", "/personal", "/incidencias", "/pagos", "/reportes", "/usuarios", "/auditoria", "/mis-tareas"];

  function requerirSesion() { if (!token() || !usuario()) { location.href = "/login.html"; return false; } return true; }

  // Rellena los <svg data-ico="..."> escritos en el HTML
  function pintarIconos(raiz = document) { raiz.querySelectorAll("svg[data-ico]").forEach(s => { s.innerHTML = ICONOS[s.dataset.ico] || ""; }); }

  function montarLayout(activa) {
    pintarIconos();
    if (!requerirSesion()) return;
    const u = usuario();
    const opciones = (u.opciones || []).slice().sort((a, b) => ORDEN.indexOf(a.ruta) - ORDEN.indexOf(b.ruta));
    const enlaces = opciones.filter(o => PAGINAS[o.ruta]).map(o => {
      const [pag, ico] = PAGINAS[o.ruta];
      return `<a href="/app/${pag}" class="${pag === activa ? "activo" : ""}" ${pag === activa ? 'aria-current="page"' : ""}>${icono(ico)}<span>${esc(o.nombre)}</span></a>`;
    }).join("");
    const sb = document.getElementById("sidebar");
    if (sb) sb.innerHTML = `<div class="sidebar-in">
      <a class="marca" href="/app/panel.html"><img src="/img/logo-isotipo-claro.svg" alt=""><span>Nido</span></a>
      <nav class="nav" aria-label="Menú principal">${enlaces}</nav>
      <nav class="nav nav-pie" aria-label="Cuenta"><div class="sep"></div>
        <a href="#" id="btn-salir">${icono("logout")}<span>Cerrar sesión</span></a></nav></div>`;
    const iniciales = (u.nombres || u.login).split(" ").map(p => p[0]).slice(0, 2).join("").toUpperCase();
    const tb = document.getElementById("topbar");
    if (tb) tb.innerHTML = `
      <label class="buscador"><span class="sr-only">Buscar</span>${icono("search")}<input class="input" type="search" id="busqueda-global" placeholder="Buscar alojamiento, huésped o reserva..."></label>
      <div class="topbar-der">
        <span class="idioma" title="Idioma: español (Perú)">${icono("globe")}ES</span>
        <a class="btn btn-fantasma btn-icono" href="/app/incidencias.html" aria-label="Incidencias abiertas">${icono("bell")}</a>
        <div class="perfil"><div class="avatar" aria-hidden="true">${esc(iniciales)}</div>
          <div><div class="nombre">${esc(u.nombres)}</div><div class="rol">${esc(titulo(u.roles[0]))}</div></div></div>
      </div>`;
    document.getElementById("btn-salir")?.addEventListener("click", e => { e.preventDefault(); cerrarSesion(); });
    document.getElementById("busqueda-global")?.addEventListener("keydown", e => {
      if (e.key === "Enter" && window.alBuscar) window.alBuscar(e.target.value.trim().toLowerCase());
    });
  }

  // Pantalla de inicio segun el rol
  const inicio = () => esSoloPersonal() ? "/app/tareas.html" : "/app/panel.html";

  const vacio = (ico, tituloTxt, texto, accion = "") =>
    `<div class="vacio">${icono(ico)}<strong>${esc(tituloTxt)}</strong><div>${esc(texto)}</div>${accion ? `<div style="margin-top:16px">${accion}</div>` : ""}</div>`;
  const esqueletoFilas = (n, cols) => Array.from({ length: n }, () => `<tr>${Array.from({ length: cols }, () => '<td><div class="esqueleto"></div></td>').join("")}</tr>`).join("");

  // ---------- Listas desplegables, fechas para inputs e imagenes protegidas ----------
  function llenarSelect(el, items, valor, texto, vacioTxt) {
    el.innerHTML = (vacioTxt !== undefined ? `<option value="">${esc(vacioTxt)}</option>` : "") +
      items.map(i => `<option value="${esc(valor(i))}">${esc(texto(i))}</option>`).join("");
  }
  const paraInput = (v) => v ? String(v).slice(0, 16) : "";
  async function imagen(ruta) {
    const r = await fetch(API + ruta, { headers: { Authorization: "Bearer " + token() } });
    if (!r.ok) throw new Error("imagen");
    return URL.createObjectURL(await r.blob());
  }
  const mesActual = () => {
    const d = new Date(), y = d.getFullYear(), m = d.getMonth();
    const f = (x) => `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, "0")}-${String(x.getDate()).padStart(2, "0")}`;
    return { desde: f(new Date(y, m, 1)), hasta: f(new Date(y, m + 1, 0)) };
  };

  return { fechaCorta, pintarIconos, llenarSelect, paraInput, imagen, mesActual, api, get, post, put, patch, del, icono, token, usuario, tieneRol, esSoloPersonal, guardarSesion, cerrarSesion,
    moneda, fecha, fechaHora, hora, esc, hoyISO, titulo, badge, toast, abrirModal, cerrarModal, confirmar,
    prepararFormulario, formularioValido, mostrarErroresApi, montarLayout, requerirSesion, inicio, vacio, esqueletoFilas };
})();
