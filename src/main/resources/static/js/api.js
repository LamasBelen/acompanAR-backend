// ==========================================
// Funciones comunes que usan todas las páginas
// ==========================================

// Hace un pedido a la API y devuelve { ok, status, datos }.
// Como el front está en el mismo servidor que el backend, el navegador
// manda solo la cookie de sesión (JSESSIONID): no hace falta configurar nada más.
async function pedir(url, metodo = "GET", cuerpo = null) {
    const opciones = {
        method: metodo,
        headers: { "Content-Type": "application/json" }
    };
    if (cuerpo !== null) {
        opciones.body = JSON.stringify(cuerpo);
    }

    const respuesta = await fetch(url, opciones);

    // La respuesta puede ser JSON, texto o estar vacía: la leemos como texto y probamos convertirla
    const texto = await respuesta.text();
    let datos = texto;
    try {
        datos = texto ? JSON.parse(texto) : null;
    } catch (e) {
        // no era JSON, dejamos el texto tal cual
    }
    return { ok: respuesta.ok, status: respuesta.status, datos: datos };
}

// Devuelve los datos del usuario logueado, o null si no hay sesión
async function obtenerUsuarioActual() {
    const resultado = await pedir("/api/auth/me");
    return resultado.ok ? resultado.datos : null;
}

// Para páginas que requieren estar logueado: si no hay sesión, manda al login
async function requerirSesion() {
    const usuario = await obtenerUsuarioActual();
    if (usuario === null) {
        window.location.href = "login.html";
    }
    return usuario;
}

async function cerrarSesion() {
    await pedir("/api/auth/logout", "POST");
    window.location.href = "index.html";
}

// Devuelve el mensaje de error que mandó el servidor (campo "mensaje"),
// o un texto por defecto si el servidor no mandó ninguno (por ejemplo, si no hay conexión).
function mensajeDeError(resultado, textoPorDefecto) {
    if (resultado && resultado.datos && typeof resultado.datos === "object" && resultado.datos.mensaje) {
        return resultado.datos.mensaje;
    }
    return textoPorDefecto;
}

// Muestra un mensaje de error o de éxito dentro de un elemento con class="mensaje"
function mostrarMensaje(elemento, texto, tipo) {
    elemento.textContent = texto; // textContent (no innerHTML) evita inyectar HTML
    elemento.className = "mensaje " + tipo;
}

// Arma el menú de la barra superior según haya o no sesión
// Pie de página común a todas las pantallas
function armarPie() {
    if (document.querySelector(".pie")) return;
    const pie = crear("footer", "pie");
    pie.appendChild(crear("img", "", "", { src: "img/simbolo.png", alt: "" }));
    const marca = crear("strong", "", "acompañAR");
    pie.appendChild(marca);
    pie.appendChild(document.createTextNode(" · Acompañar, cuidar, incluir · Río Grande, Tierra del Fuego"));
    document.body.appendChild(pie);
}

async function armarMenu() {
    armarPie();
    const menu = document.getElementById("menu");
    if (!menu) return;
    armarBotonMenu(menu);

    const usuario = await obtenerUsuarioActual();
    menu.innerHTML = "";

    if (usuario === null) {
        menu.innerHTML = '<a href="login.html">Ingresar</a>' +
            '<a class="boton" href="registro.html">Registrarme</a>';
    } else {
        // Enlaces que ve cada rol (se van sumando a medida que armamos las pantallas)
        const enlacesPorRol = {
            PROFESIONAL: [
                { texto: "Ofertas", url: "ofertas.html" },
                { texto: "Mis postulaciones", url: "mis-postulaciones.html" },
                { texto: "Mensajes", url: "mensajes.html" },
                { texto: "Capacitaciones", url: "capacitaciones.html" },
                { texto: "Mis capacitaciones", url: "mis-capacitaciones.html" },
                { texto: "Mi perfil", url: "perfil.html" }
            ],
            EMPLEADOR: [
                { texto: "Mis ofertas", url: "mis-ofertas.html" },
                { texto: "Publicar oferta", url: "oferta-form.html" },
                { texto: "Mensajes", url: "mensajes.html" },
                { texto: "Mi perfil", url: "perfil.html" }
            ],
            INSTITUCION: [
                { texto: "Mis capacitaciones", url: "admin-capacitaciones.html" },
                { texto: "Nueva capacitación", url: "capacitacion-form.html" }
            ],
            ADMINISTRADOR: [
                { texto: "Moderación", url: "admin-moderacion.html" },
                { texto: "Capacitaciones", url: "admin-capacitaciones.html" },
                { texto: "Nueva capacitación", url: "capacitacion-form.html" },
                { texto: "Catálogos", url: "admin-catalogos.html" }
            ]
        };
        const enlaces = enlacesPorRol[usuario.rol] || [];
        enlaces.forEach(function (e) {
            // El enlace de la página actual se resalta
            const esActual = window.location.pathname.endsWith("/" + e.url);
            const enlace = crear("a", esActual ? "activo" : "", e.texto, { href: e.url });
            menu.appendChild(enlace);
            // El enlace de mensajes muestra cuántos mensajes sin leer hay (y se actualiza cada 30 segundos)
            if (e.url === "mensajes.html") {
                actualizarGloboMensajes(enlace);
                setInterval(function () { actualizarGloboMensajes(enlace); }, 30000);
            }
        });

        armarCampana(menu);
        menu.appendChild(crear("a", "", usuario.nombreUsuario, { href: "inicio.html" }));

        const boton = crear("button", "boton boton-secundario", "Cerrar sesión");
        boton.addEventListener("click", cerrarSesion);
        menu.appendChild(boton);
    }
}

// Crea un elemento HTML de forma segura (el texto se pone con textContent, no con innerHTML).
// Ejemplo: crear("p", "detalle", "Hola") -> <p class="detalle">Hola</p>
function crear(etiqueta, clase, texto, atributos) {
    const elemento = document.createElement(etiqueta);
    if (clase) elemento.className = clase;
    if (texto !== undefined && texto !== null) elemento.textContent = texto;
    if (atributos) {
        Object.keys(atributos).forEach(function (nombre) {
            elemento.setAttribute(nombre, atributos[nombre]);
        });
    }
    return elemento;
}

// Crea un enlace solo si la dirección empieza con http:// o https://.
// Es una medida de seguridad: evita enlaces tipo "javascript:..." cargados por un usuario malintencionado.
// Si la dirección no es válida, devuelve null.
function crearEnlaceSeguro(url, texto) {
    if (!url || !/^https?:\/\//i.test(url)) return null;
    return crear("a", "", texto, { href: url, target: "_blank", rel: "noopener noreferrer" });
}

// ------------------------------------------
// Listas desplegables (select) que vienen del servidor
// ------------------------------------------

// Llena un <select> con una lista de opciones [{ valor, texto }]
function llenarSelect(select, opciones, textoInicial) {
    select.innerHTML = "";
    if (textoInicial) {
        select.appendChild(crear("option", "", textoInicial, { value: "" }));
    }
    opciones.forEach(function (opcion) {
        select.appendChild(crear("option", "", opcion.texto, { value: opcion.valor }));
    });
}

// Localidades permitidas (enum del servidor). Se piden una sola vez y se guardan.
let localidadesGuardadas = [];
async function cargarLocalidades() {
    if (localidadesGuardadas.length === 0) {
        const resultado = await pedir("/api/catalogos/localidades");
        if (resultado.ok) localidadesGuardadas = resultado.datos;
    }
    return localidadesGuardadas;
}

// Devuelve el nombre lindo de una localidad: "RIO_GRANDE" -> "Río Grande"
// (hay que haber llamado antes a cargarLocalidades)
function nombreLocalidad(codigo) {
    const encontrada = localidadesGuardadas.find(function (l) { return l.codigo === codigo; });
    return encontrada ? encontrada.nombre : codigo;
}

// Rellena un <select> con las localidades
async function llenarLocalidades(select) {
    const lista = await cargarLocalidades();
    llenarSelect(select, lista.map(function (l) { return { valor: l.codigo, texto: l.nombre }; }));
}

// Rellena un <select> con los tipos de profesional activos
async function llenarTiposProfesional(select) {
    const resultado = await pedir("/api/tipos-profesional");
    const lista = resultado.ok ? resultado.datos : [];
    llenarSelect(select, lista.map(function (t) { return { valor: t.id, texto: t.nombre }; }));
}

// Convierte una fecha "2026-10-28" en "28/10/2026"
function formatearFecha(fecha) {
    if (!fecha) return "";
    const partes = fecha.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

// ------------------------------------------
// Calificación mutua (profesional <-> empleador)
// ------------------------------------------

// Ids de las postulaciones que el usuario logueado ya calificó
async function cargarCalificacionesHechas() {
    const resultado = await pedir("/api/calificaciones/mias");
    return new Set(resultado.ok ? resultado.datos : []);
}

// Reputación de un usuario: { promedio, cantidad, calificaciones }
async function obtenerReputacion(idUsuario) {
    const resultado = await pedir("/api/calificaciones/usuario/" + idUsuario);
    return resultado.ok ? resultado.datos : null;
}

// Texto corto: "★ 4.5 (3 calificaciones)"
function textoReputacion(reputacion) {
    if (!reputacion || reputacion.cantidad === 0) return "Sin calificaciones todavía";
    return "★ " + reputacion.promedio.toFixed(1) + " (" + reputacion.cantidad +
        (reputacion.cantidad === 1 ? " calificación)" : " calificaciones)");
}

// Arma el formulario para calificar una postulación aceptada.
// alTerminar se ejecuta cuando la calificación se guardó bien (para recargar la lista).
function armarFormularioCalificacion(idPostulacion, alTerminar) {
    const formulario = crear("form", "calificar");

    formulario.appendChild(crear("label", "", "Puntaje"));
    const puntaje = crear("select");
    llenarSelect(puntaje, [
        { valor: 5, texto: "5 - Excelente" },
        { valor: 4, texto: "4 - Muy bueno" },
        { valor: 3, texto: "3 - Bueno" },
        { valor: 2, texto: "2 - Regular" },
        { valor: 1, texto: "1 - Malo" }
    ]);
    formulario.appendChild(puntaje);

    formulario.appendChild(crear("label", "", "Comentario (opcional)"));
    const comentario = crear("textarea", "", "", { maxlength: "500" });
    formulario.appendChild(comentario);

    const aviso = crear("div", "mensaje");
    const enviar = crear("button", "boton boton-chico", "Enviar calificación", { type: "submit" });
    formulario.appendChild(enviar);
    formulario.appendChild(aviso);

    formulario.addEventListener("submit", async function (evento) {
        evento.preventDefault();
        enviar.disabled = true;
        const resultado = await pedir("/api/calificaciones/nueva", "POST", {
            idPostulacion: idPostulacion,
            puntaje: parseInt(puntaje.value, 10),
            comentario: comentario.value
        });
        if (resultado.ok) {
            alTerminar();
        } else {
            enviar.disabled = false;
            mostrarMensaje(aviso, mensajeDeError(resultado, "No se pudo guardar la calificación."), "error");
        }
    });
    return formulario;
}

// Muestra las calificaciones recibidas dentro de un contenedor (se usa en el perfil)
function mostrarCalificacionesRecibidas(contenedor, reputacion) {
    contenedor.innerHTML = "";
    contenedor.appendChild(crear("p", "reputacion", textoReputacion(reputacion)));
    if (!reputacion || reputacion.cantidad === 0) return;
    reputacion.calificaciones.forEach(function (c) {
        const bloque = crear("div", "comentario");
        bloque.appendChild(crear("p", "detalle",
            estrellas(c.puntaje) + " · " + c.nombreAutor + " · " + c.tituloOferta + " · " + formatearFecha(c.fecha)));
        if (c.comentario) bloque.appendChild(crear("p", "", c.comentario));
        contenedor.appendChild(bloque);
    });
}

// Botón "Ver opiniones" que despliega, en la misma tarjeta, las estrellas y los comentarios que recibió un usuario.
// Sirve para decidir (aceptar a un profesional o postularse a un empleador) sin salir de la lista.
function agregarOpiniones(info, idUsuario) {
    const boton = crear("button", "boton-enlace boton-opiniones", "Ver opiniones", { type: "button" });
    const zona = crear("div", "opiniones");
    zona.style.display = "none";
    let cargada = false;
    boton.addEventListener("click", async function () {
        const visible = zona.style.display !== "none";
        zona.style.display = visible ? "none" : "block";
        boton.textContent = visible ? "Ver opiniones" : "Ocultar opiniones";
        if (!visible && !cargada) {
            cargada = true;
            mostrarCalificacionesRecibidas(zona, await obtenerReputacion(idUsuario));
        }
    });
    info.appendChild(boton);
    info.appendChild(zona);
}

// ------------------------------------------
// Denuncias (moderación de la comunidad)
// ------------------------------------------

// Motivos que coinciden con el enum MotivoDenuncia del servidor
const MOTIVOS_DENUNCIA = [
    { valor: "ESTAFA", texto: "Posible estafa" },
    { valor: "CONTENIDO_INAPROPIADO", texto: "Contenido inapropiado o que no es laboral" },
    { valor: "DATOS_FALSOS", texto: "Datos falsos" },
    { valor: "OTRO", texto: "Otro motivo" }
];

// Arma el formulario para denunciar algo.
// objeto: PROFESIONAL, EMPLEADOR, INSTITUCION, OFERTA o CAPACITACION. alTerminar se ejecuta al enviar bien.
function armarFormularioDenuncia(objeto, idObjeto, alTerminar) {
    const formulario = crear("form", "calificar");

    formulario.appendChild(crear("label", "", "Motivo de la denuncia"));
    const motivo = crear("select");
    llenarSelect(motivo, MOTIVOS_DENUNCIA);
    formulario.appendChild(motivo);

    formulario.appendChild(crear("label", "", "Contanos qué pasó"));
    const descripcion = crear("textarea", "", "", { maxlength: "500", required: "required" });
    formulario.appendChild(descripcion);

    const aviso = crear("div", "mensaje");
    const enviar = crear("button", "boton boton-chico boton-peligro", "Enviar denuncia", { type: "submit" });
    const cancelar = crear("button", "boton boton-chico boton-secundario", "Cancelar", { type: "button" });
    cancelar.addEventListener("click", function () { alTerminar(false); });
    formulario.appendChild(enviar);
    formulario.appendChild(cancelar);
    formulario.appendChild(aviso);

    formulario.addEventListener("submit", async function (evento) {
        evento.preventDefault();
        enviar.disabled = true;
        const resultado = await pedir("/api/denuncias/nueva", "POST", {
            objeto: objeto,
            idObjeto: idObjeto,
            motivo: motivo.value,
            descripcion: descripcion.value.trim()
        });
        if (resultado.ok) {
            alTerminar(true);
        } else {
            enviar.disabled = false;
            mostrarMensaje(aviso, mensajeDeError(resultado, "No se pudo enviar la denuncia."), "error");
        }
    });
    return formulario;
}

// Agrega un botón "Denunciar" a una tarjeta: al apretarlo aparece el formulario dentro de "info".
function agregarBotonDenuncia(acciones, info, objeto, idObjeto) {
    const boton = crear("button", "boton-enlace", "Denunciar");
    boton.addEventListener("click", function () {
        boton.disabled = true;
        const formulario = armarFormularioDenuncia(objeto, idObjeto, function (enviada) {
            formulario.remove();
            if (enviada) {
                boton.textContent = "Denuncia enviada ✓";
            } else {
                boton.disabled = false;
            }
        });
        info.appendChild(formulario);
    });
    acciones.appendChild(boton);
}

// ------------------------------------------
// Inscriptos a una capacitación (para la institución)
// ------------------------------------------

// Lista de inscriptos de una capacitación: [{ nombreCompleto, tipoProfesional, email, telefono, estado, fechaInscripcion }]
async function cargarInscriptos(idCapacitacion) {
    const resultado = await pedir("/api/inscripciones/capacitacion/" + idCapacitacion);
    return resultado.ok && Array.isArray(resultado.datos) ? resultado.datos : [];
}

// Las inscripciones canceladas no cuentan como inscriptos
function inscriptosVigentes(lista) {
    return lista.filter(function (i) { return i.estado !== "CANCELADA"; });
}

// "3 inscriptos" / "1 inscripto" / "Sin inscriptos"
function textoInscriptos(cantidad) {
    if (cantidad === 0) return "Sin inscriptos";
    return cantidad + (cantidad === 1 ? " inscripto" : " inscriptos");
}

// Dibuja la lista de inscriptos dentro de un contenedor
function mostrarInscriptos(contenedor, lista) {
    contenedor.innerHTML = "";
    if (lista.length === 0) {
        contenedor.appendChild(crear("p", "detalle", "Todavía no se inscribió nadie."));
        return;
    }
    lista.forEach(function (i) {
        const bloque = crear("div", "comentario");
        bloque.appendChild(crear("p", "", i.nombreCompleto + (i.tipoProfesional ? " · " + i.tipoProfesional : "")));
        const contacto = [i.email, i.telefono].filter(Boolean).join(" · ");
        bloque.appendChild(crear("p", "detalle",
            (contacto ? contacto + " · " : "") + "Se inscribió el " + formatearFecha(i.fechaInscripcion) + " · " + i.estado));
        contenedor.appendChild(bloque);
    });
}

// ------------------------------------------
// Ventana de confirmación (modal)
// ------------------------------------------

// Muestra una ventana para confirmar una acción y devuelve una promesa: true si confirma, false si cancela.
// Se usa con await:  if (!await confirmarAccion("¿Seguro?")) return;
// opciones (todas opcionales): titulo, textoBoton, peligro (true = botón rojo, es lo normal para bajas y rechazos).
// El foco arranca en "Cancelar" para que apretar Enter por error no confirme nada.
function confirmarAccion(texto, opciones) {
    const config = Object.assign({ titulo: "¿Confirmás esta acción?", textoBoton: "Sí, confirmar", peligro: true }, opciones || {});

    return new Promise(function (resolver) {
        const foco = document.activeElement;
        const fondo = crear("div", "modal-fondo");
        const ventana = crear("div", "modal", "", {
            role: "dialog", "aria-modal": "true", "aria-labelledby": "modalTitulo", "aria-describedby": "modalTexto"
        });
        ventana.appendChild(crear("h2", "", config.titulo, { id: "modalTitulo" }));
        ventana.appendChild(crear("p", "", texto, { id: "modalTexto" }));
        // Contenido extra opcional (un resumen armado con crear(), nunca con innerHTML)
        if (config.contenido) ventana.appendChild(config.contenido);

        const botones = crear("div", "modal-botones");
        const cancelar = crear("button", "boton boton-secundario", "Cancelar", { type: "button" });
        const aceptar = crear("button", "boton" + (config.peligro ? " boton-peligro" : ""), config.textoBoton, { type: "button" });
        botones.appendChild(cancelar);
        botones.appendChild(aceptar);
        ventana.appendChild(botones);
        fondo.appendChild(ventana);

        function cerrar(respuesta) {
            document.removeEventListener("keydown", teclas);
            fondo.remove();
            if (foco && foco.focus) foco.focus();
            resolver(respuesta);
        }

        // Escape cancela; Tab se queda dentro de la ventana
        function teclas(evento) {
            if (evento.key === "Escape") {
                cerrar(false);
            } else if (evento.key === "Tab") {
                evento.preventDefault();
                (document.activeElement === cancelar ? aceptar : cancelar).focus();
            }
        }

        cancelar.addEventListener("click", function () { cerrar(false); });
        aceptar.addEventListener("click", function () { cerrar(true); });
        fondo.addEventListener("click", function (evento) { if (evento.target === fondo) cerrar(false); });
        document.addEventListener("keydown", teclas);

        document.body.appendChild(fondo);
        cancelar.focus();
    });
}

// ------------------------------------------
// Estrellas y detalles de una oferta
// ------------------------------------------

// 4 -> "★★★★☆"
function estrellas(puntaje) {
    const llenas = Math.max(0, Math.min(5, Math.round(puntaje)));
    return "★".repeat(llenas) + "☆".repeat(5 - llenas);
}

// Agrega a la tarjeta de una oferta: requisitos comunes (como etiquetas), otros requisitos y período del trabajo
// Si se pasa requisitosPersona (los que cumple el profesional), los requisitos se marcan como cumplidos o faltantes.
function agregarDetallesOferta(info, oferta, requisitosPersona, esEmpleador) {
    if (requisitosPersona !== undefined) {
        agregarCoincidencia(info, oferta.requisitosComunes, requisitosPersona, esEmpleador);
    } else if (oferta.requisitosComunes && oferta.requisitosComunes.length > 0) {
        const etiquetas = crear("div", "etiquetas");
        oferta.requisitosComunes.forEach(function (r) {
            etiquetas.appendChild(crear("span", "etiqueta", r.nombre));
        });
        info.appendChild(etiquetas);
    }
    if (oferta.requisitos) {
        info.appendChild(crear("p", "detalle", "Otros requisitos: " + oferta.requisitos));
    }
    if (oferta.fechaInicio && oferta.duracionSemanas) {
        info.appendChild(crear("p", "detalle",
            "Inicio: " + formatearFecha(oferta.fechaInicio) + " · Duración: " + oferta.duracionSemanas +
            (oferta.duracionSemanas === 1 ? " semana" : " semanas")));
    }
}

// ------------------------------------------
// Coincidencia de requisitos
// ------------------------------------------

// Compara los requisitos que pide una oferta con los que cumple un profesional.
// Devuelve { total, cumplidos: [...], faltantes: [...] }. Es solo para mostrar; no decide nada.
function calcularCoincidencia(requisitosOferta, requisitosPersona) {
    const tiene = new Set((requisitosPersona || []).map(function (r) { return r.id; }));
    const cumplidos = [];
    const faltantes = [];
    (requisitosOferta || []).forEach(function (r) {
        (tiene.has(r.id) ? cumplidos : faltantes).push(r);
    });
    return { total: cumplidos.length + faltantes.length, cumplidos: cumplidos, faltantes: faltantes };
}

// Dibuja "Cumplís 2 de 3 requisitos" y cada requisito como etiqueta verde (cumple) o naranja (falta).
// esEmpleador cambia solo la redacción: "Cumple" en vez de "Cumplís".
function agregarCoincidencia(info, requisitosOferta, requisitosPersona, esEmpleador) {
    const c = calcularCoincidencia(requisitosOferta, requisitosPersona);
    if (c.total === 0) return;

    const verbo = esEmpleador ? "Cumple" : "Cumplís";
    const clase = c.faltantes.length === 0 ? "coincidencia coincidencia-total" : "coincidencia";
    info.appendChild(crear("p", clase, verbo + " " + c.cumplidos.length + " de " + c.total +
        (c.total === 1 ? " requisito" : " requisitos") + (c.faltantes.length === 0 ? " ✓" : "")));

    const etiquetas = crear("div", "etiquetas");
    c.cumplidos.forEach(function (r) { etiquetas.appendChild(crear("span", "etiqueta cumple", "✓ " + r.nombre)); });
    c.faltantes.forEach(function (r) { etiquetas.appendChild(crear("span", "etiqueta falta", "Falta: " + r.nombre)); });
    info.appendChild(etiquetas);
}

// Casillas con los requisitos del catálogo (las usan el formulario de ofertas y los perfiles de profesionales).
// seleccionados es la lista de ids que empiezan tildados.
async function llenarCasillasRequisitos(contenedor, seleccionados) {
    const resultado = await pedir("/api/requisitos");
    contenedor.innerHTML = "";
    (resultado.ok ? resultado.datos : []).forEach(function (r) {
        const etiqueta = crear("label", "opcion-requisito");
        const casilla = crear("input", "", "", { type: "checkbox", value: r.id });
        casilla.checked = seleccionados.includes(r.id);
        etiqueta.appendChild(casilla);
        etiqueta.appendChild(crear("span", "", r.nombre));
        contenedor.appendChild(etiqueta);
    });
    convertirEnDesplegable(contenedor);
}

// Convierte la lista de casillas en un desplegable: un botón que abre la lista y, debajo,
// las etiquetas de lo que se eligió. Así el formulario no se estira. Las casillas siguen
// dentro del mismo contenedor, por eso leerCasillasRequisitos() funciona igual.
function convertirEnDesplegable(contenedor) {
    if (contenedor.dataset.desplegable === "si") {
        contenedor.dispatchEvent(new Event("change"));
        return;
    }
    contenedor.dataset.desplegable = "si";

    const envoltorio = crear("div", "desplegable");
    const boton = crear("button", "desplegable-boton", "", { type: "button", "aria-expanded": "false" });
    const textoBoton = crear("span");
    boton.appendChild(textoBoton);
    boton.appendChild(crear("span", "desplegable-flecha", "▾", { "aria-hidden": "true" }));
    const elegidos = crear("div", "etiquetas desplegable-elegidos");

    contenedor.parentNode.insertBefore(envoltorio, contenedor);
    const caja = crear("div", "desplegable-caja");
    caja.appendChild(boton);
    caja.appendChild(contenedor);
    envoltorio.appendChild(caja);
    envoltorio.appendChild(elegidos);
    contenedor.classList.add("desplegable-panel");
    contenedor.hidden = true;

    function actualizar() {
        const tildadas = Array.from(contenedor.querySelectorAll("input:checked"));
        textoBoton.textContent = tildadas.length === 0
            ? "Elegir requisitos"
            : "Elegir requisitos (" + tildadas.length + " seleccionado" + (tildadas.length === 1 ? "" : "s") + ")";
        elegidos.innerHTML = "";
        tildadas.forEach(function (c) {
            elegidos.appendChild(crear("span", "etiqueta", c.parentNode.textContent));
        });
    }
    function alternar(abrir) {
        contenedor.hidden = !abrir;
        boton.setAttribute("aria-expanded", abrir ? "true" : "false");
    }

    boton.addEventListener("click", function () { alternar(contenedor.hidden); });
    contenedor.addEventListener("change", actualizar);
    document.addEventListener("click", function (e) {
        if (!envoltorio.contains(e.target)) alternar(false);
    });
    envoltorio.addEventListener("keydown", function (e) {
        if (e.key === "Escape") { alternar(false); boton.focus(); }
    });
    actualizar();
}

// Ids de los requisitos tildados
function leerCasillasRequisitos(contenedor) {
    return Array.from(contenedor.querySelectorAll("input:checked"))
        .map(function (c) { return parseInt(c.value, 10); });
}

// ------------------------------------------
// Notificaciones (campanita del menú)
// ------------------------------------------

// "2026-11-14T21:30:00" -> "14/11 21:30"
function formatearFechaHora(texto) {
    const f = new Date(texto);
    if (isNaN(f.getTime())) return "";
    const dos = function (n) { return String(n).padStart(2, "0"); };
    return dos(f.getDate()) + "/" + dos(f.getMonth() + 1) + " " + dos(f.getHours()) + ":" + dos(f.getMinutes());
}

// Agrega la campanita al menú: muestra cuántos avisos nuevos hay y, al tocarla, la lista.
function armarCampana(menu) {
    const contenedor = crear("div", "campana-contenedor");
    const boton = crear("button", "campana", "", { type: "button", "aria-label": "Notificaciones", "aria-expanded": "false" });
    boton.innerHTML = '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" ' +
        'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>' +
        '<path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>';
    const numero = crear("span", "campana-numero");
    numero.style.display = "none";
    boton.appendChild(numero);

    const panel = crear("div", "panel-notificaciones");
    panel.style.display = "none";
    contenedor.appendChild(boton);
    contenedor.appendChild(panel);
    menu.appendChild(contenedor);

    async function actualizarNumero() {
        const r = await pedir("/api/notificaciones/no-leidas");
        const cantidad = r.ok && r.datos ? r.datos.cantidad : 0;
        numero.textContent = cantidad > 9 ? "9+" : String(cantidad);
        numero.style.display = cantidad > 0 ? "flex" : "none";
        boton.setAttribute("aria-label", cantidad > 0 ? "Notificaciones: " + cantidad + " sin leer" : "Notificaciones");
    }

    async function llenarPanel() {
        const r = await pedir("/api/notificaciones");
        panel.innerHTML = "";

        const cabecera = crear("div", "panel-cabecera");
        cabecera.appendChild(crear("strong", "", "Notificaciones"));
        const todas = crear("button", "enlace-boton", "Marcar todas como leídas", { type: "button" });
        todas.addEventListener("click", async function () {
            await pedir("/api/notificaciones/leer-todas", "PUT");
            await llenarPanel();
            actualizarNumero();
        });
        cabecera.appendChild(todas);
        panel.appendChild(cabecera);

        const avisos = r.ok && r.datos ? r.datos : [];
        if (avisos.length === 0) {
            panel.appendChild(crear("p", "panel-vacio", "No tenés notificaciones."));
            return;
        }
        const lista = crear("ul", "panel-lista");
        avisos.forEach(function (aviso) {
            const item = crear("li");
            const enlace = crear("button", "aviso" + (aviso.leida ? "" : " aviso-nuevo"), "", { type: "button" });
            enlace.appendChild(crear("span", "aviso-texto", aviso.mensaje));
            enlace.appendChild(crear("small", "", formatearFechaHora(aviso.fecha)));
            enlace.addEventListener("click", async function () {
                if (!aviso.leida) await pedir("/api/notificaciones/leida/" + aviso.id, "PUT");
                // Solo se navega a pantallas propias de la app (no a direcciones externas)
                if (aviso.url && /^[a-z0-9-]+\.html(\?[A-Za-z0-9=&_-]*)?$/i.test(aviso.url)) {
                    window.location.href = aviso.url;
                } else {
                    await llenarPanel();
                    actualizarNumero();
                }
            });
            item.appendChild(enlace);
            lista.appendChild(item);
        });
        panel.appendChild(lista);
    }

    function cerrar() {
        panel.style.display = "none";
        boton.setAttribute("aria-expanded", "false");
    }

    boton.addEventListener("click", async function (evento) {
        evento.stopPropagation();
        if (panel.style.display === "none") {
            panel.style.display = "block";
            boton.setAttribute("aria-expanded", "true");
            await llenarPanel();
        } else {
            cerrar();
        }
    });
    document.addEventListener("click", function (evento) {
        if (!contenedor.contains(evento.target)) cerrar();
    });
    document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape") cerrar();
    });

    actualizarNumero();
    // Se revisa si hay avisos nuevos cada minuto
    setInterval(function () {
        if (panel.style.display === "none") actualizarNumero();
    }, 60000);
}

// ------------------------------------------
// Progreso del trabajo (semana por semana)
// ------------------------------------------

const TEXTO_SEMANA = { COMPLETADA: "Completada ✓", EN_CURSO: "En curso", PENDIENTE: "Pendiente" };
const TEXTO_PROGRESO = { POR_COMENZAR: "Por comenzar", EN_CURSO: "En curso", FINALIZADO: "Finalizado" };

// Dibuja dentro de "contenedor" el avance del trabajo de una postulación aceptada.
// Lo ven el profesional y el empleador. El servidor calcula todo a partir de las fechas de la oferta.
async function mostrarProgreso(contenedor, idPostulacion) {
    const resultado = await pedir("/api/postulaciones/" + idPostulacion + "/progreso");
    contenedor.innerHTML = "";
    if (!resultado.ok) return;
    const p = resultado.datos;

    const bloque = crear("div", "progreso");
    if (p.estado === "SIN_FECHAS") {
        bloque.appendChild(crear("p", "detalle", "Esta oferta no definió fecha de inicio ni duración, por eso no hay progreso para mostrar."));
        contenedor.appendChild(bloque);
        return;
    }

    bloque.appendChild(crear("p", "progreso-titulo",
        "Progreso del trabajo · " + TEXTO_PROGRESO[p.estado] + " (" + p.porcentaje + "%)"));
    bloque.appendChild(crear("p", "detalle",
        "Del " + formatearFecha(p.fechaInicio) + " al " + formatearFecha(p.fechaFin) + " · " +
        p.semanasCompletadas + " de " + p.totalSemanas + " semanas completadas"));

    const barra = crear("div", "barra-progreso", "", {
        role: "progressbar", "aria-valuemin": "0", "aria-valuemax": "100", "aria-valuenow": String(p.porcentaje)
    });
    const relleno = crear("div", "barra-relleno");
    relleno.style.width = p.porcentaje + "%";
    barra.appendChild(relleno);
    bloque.appendChild(barra);

    const semanas = crear("ol", "semanas");
    p.semanas.forEach(function (semana) {
        const item = crear("li", "semana semana-" + semana.estado);
        item.appendChild(crear("strong", "", "Semana " + semana.numero));
        item.appendChild(crear("span", "", TEXTO_SEMANA[semana.estado]));
        item.appendChild(crear("small", "", formatearFecha(semana.desde) + " al " + formatearFecha(semana.hasta)));
        semanas.appendChild(item);
    });
    bloque.appendChild(semanas);
    contenedor.appendChild(bloque);
}


// ------------------------------------------
// Piezas visuales compartidas: avatar, etiqueta de estado, tarjeta con avatar
// ------------------------------------------

// Círculo con las iniciales (para personas y empresas). Es decorativo: el nombre ya está escrito al lado.
function crearAvatar(nombre, apellido) {
    const palabras = ((nombre || "") + " " + (apellido || "")).trim().split(/\s+/).filter(function (p) { return p.length > 0; });
    let iniciales = "";
    if (palabras.length > 0) iniciales += palabras[0].charAt(0);
    if (palabras.length > 1) iniciales += palabras[1].charAt(0);
    const avatar = crear("span", "avatar", iniciales.toUpperCase() || "?", { "aria-hidden": "true" });
    // Dos tonos de la paleta, según la letra inicial, para que no sean todos iguales
    if (iniciales && iniciales.toUpperCase().charCodeAt(0) % 2 === 1) avatar.classList.add("avatar-naranja");
    return avatar;
}

// Etiqueta de estado con un puntito de color y el texto en minúscula legible ("Pendiente", "Aceptada")
const NOMBRES_ESTADO = { CONFIRMADA: "En curso" };
function crearEtiquetaEstado(estado) {
    const texto = NOMBRES_ESTADO[estado] || (estado.charAt(0) + estado.slice(1).toLowerCase());
    return crear("span", "estado estado-" + estado, texto);
}

// Arma el cuerpo de una tarjeta: avatar a la izquierda y la información a la derecha
function conAvatar(avatar, info) {
    const fila = crear("div", "con-avatar");
    fila.appendChild(avatar);
    info.classList.add("con-avatar-info");
    fila.appendChild(info);
    return fila;
}


// ------------------------------------------
// Menú tipo hamburguesa (solo se ve en pantallas angostas, ver estilos.css)
// ------------------------------------------
function armarBotonMenu(menu) {
    if (document.getElementById("botonMenu")) return;
    const boton = crear("button", "boton-menu", "", {
        id: "botonMenu", type: "button", "aria-label": "Abrir el menú", "aria-expanded": "false", "aria-controls": "menu"
    });
    boton.innerHTML = '<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" aria-hidden="true">' +
        '<path d="M4 7h16M4 12h16M4 17h16"/></svg>';

    function alternar(abrir) {
        menu.classList.toggle("abierto", abrir);
        boton.setAttribute("aria-expanded", abrir ? "true" : "false");
        boton.setAttribute("aria-label", abrir ? "Cerrar el menú" : "Abrir el menú");
    }
    boton.addEventListener("click", function () { alternar(!menu.classList.contains("abierto")); });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape") alternar(false); });
    // Al tocar un enlace del menú, se cierra
    menu.addEventListener("click", function (e) { if (e.target.closest("a")) alternar(false); });
    menu.before(boton);
}


// ------------------------------------------
// Trabajo aceptado: finalizar y mensajería
// ------------------------------------------

// Mensajes sin leer del usuario, como un Map: idPostulacion -> cantidad
async function cargarMensajesPendientes() {
    const resultado = await pedir("/api/mensajes/pendientes");
    const mapa = new Map();
    if (resultado.ok && Array.isArray(resultado.datos)) {
        resultado.datos.forEach(function (p) { mapa.set(p.idPostulacion, p.cantidad); });
    }
    return mapa;
}

// Botón "Finalizar trabajo" (lo pueden usar las dos partes). Pide confirmación y, al terminar, llama a alTerminar().
function agregarBotonFinalizar(acciones, idPostulacion, tituloOferta, alTerminar, elementoMensajes) {
    const boton = crear("button", "boton boton-chico boton-naranja", "Finalizar trabajo");
    boton.addEventListener("click", async function () {
        const confirmado = await confirmarAccion(
            "Vas a dar por terminado el trabajo «" + tituloOferta + "». Se habilita la calificación entre ustedes " +
            "y la conversación queda solo de lectura. No se puede deshacer.",
            { titulo: "¿Finalizar el trabajo?", textoBoton: "Sí, finalizar", peligro: false });
        if (!confirmado) return;
        const resultado = await pedir("/api/postulaciones/finalizar/" + idPostulacion, "PUT");
        if (resultado.ok) {
            alTerminar();
        } else {
            mostrarMensaje(elementoMensajes, mensajeDeError(resultado, "No se pudo finalizar el trabajo."), "error");
        }
    });
    acciones.appendChild(boton);
}

// Arma el chat de una postulación (lista de mensajes + formulario). Lo usan el botón "Mensajes" de cada tarjeta
// y la bandeja de mensajes. Devuelve { panel, abrir(), cerrar() }: abrir() empieza a consultar mensajes nuevos
// cada 8 segundos y cerrar() deja de hacerlo. alLeer() se llama cada vez que se cargan los mensajes (ya quedan leídos).
// estado: "ACEPTADA" (se puede escribir) o "FINALIZADA" (solo lectura).
function armarChat(idPostulacion, estado, alLeer, alEnviar) {
    const panel = crear("div", "chat");
    const lista = crear("div", "chat-lista", "", { "aria-live": "polite" });
    const error = crear("p", "chat-error");
    panel.appendChild(lista);
    panel.appendChild(error);

    let temporizador = null;
    let ultimaCantidad = -1;

    async function cargarMensajes() {
        // Si el panel se sacó de la pantalla, se deja de consultar
        if (!panel.isConnected) { clearInterval(temporizador); return; }
        const resultado = await pedir("/api/mensajes/postulacion/" + idPostulacion);
        if (!resultado.ok) {
            error.textContent = mensajeDeError(resultado, "No se pudo cargar la conversación.");
            return;
        }
        error.textContent = "";
        if (alLeer) alLeer();
        if (resultado.datos.length === ultimaCantidad) return; // nada nuevo
        ultimaCantidad = resultado.datos.length;

        lista.innerHTML = "";
        if (resultado.datos.length === 0) {
            lista.appendChild(crear("p", "detalle chat-vacio", "Todavía no hay mensajes. Escribí el primero para coordinar el trabajo."));
        }
        resultado.datos.forEach(function (m) {
            const burbuja = crear("div", "chat-burbuja" + (m.mio ? " mio" : ""));
            burbuja.appendChild(crear("p", "chat-texto", m.texto));
            burbuja.appendChild(crear("span", "chat-meta", (m.mio ? "Vos" : m.nombreAutor) + " · " + formatearFechaHora(m.fecha)));
            lista.appendChild(burbuja);
        });
        lista.scrollTop = lista.scrollHeight;
    }

    if (estado === "ACEPTADA") {
        const formulario = crear("form", "chat-formulario");
        const texto = crear("textarea", "", "", { maxlength: "1000", rows: "2", placeholder: "Escribí un mensaje...", "aria-label": "Mensaje" });
        const enviar = crear("button", "boton boton-chico", "Enviar", { type: "submit" });
        formulario.appendChild(texto);
        formulario.appendChild(enviar);
        formulario.addEventListener("submit", async function (evento) {
            evento.preventDefault();
            const contenido = texto.value.trim();
            if (contenido === "") return;
            enviar.disabled = true;
            const resultado = await pedir("/api/mensajes/postulacion/" + idPostulacion, "POST", { texto: contenido });
            enviar.disabled = false;
            if (resultado.ok) {
                texto.value = "";
                ultimaCantidad = -1;
                await cargarMensajes();
                if (alEnviar) alEnviar();
            } else {
                error.textContent = mensajeDeError(resultado, "No se pudo enviar el mensaje.");
            }
        });
        // Enter envía; Shift+Enter hace un salto de línea
        texto.addEventListener("keydown", function (e) {
            if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); formulario.requestSubmit(); }
        });
        panel.appendChild(formulario);
    } else {
        panel.appendChild(crear("p", "detalle chat-aviso", "El trabajo finalizó: la conversación quedó solo de lectura."));
    }

    return {
        panel: panel,
        abrir: function () {
            clearInterval(temporizador);
            ultimaCantidad = -1;
            cargarMensajes();
            temporizador = setInterval(cargarMensajes, 8000);
        },
        cerrar: function () { clearInterval(temporizador); }
    };
}

// Chat desplegable dentro de una tarjeta. El botón va en "acciones" y el panel se despliega dentro de "info".
// noLeidos: cantidad para el globito del botón.
function agregarChat(acciones, info, idPostulacion, estado, noLeidos) {
    const boton = crear("button", "boton boton-chico boton-secundario boton-mensajes", "Mensajes");
    const globito = crear("span", "globito", String(noLeidos || 0));
    globito.style.display = noLeidos > 0 ? "inline-flex" : "none";
    boton.appendChild(globito);

    const chat = armarChat(idPostulacion, estado, function () { globito.style.display = "none"; });
    chat.panel.hidden = true;

    boton.addEventListener("click", function () {
        chat.panel.hidden = !chat.panel.hidden;
        boton.setAttribute("aria-expanded", chat.panel.hidden ? "false" : "true");
        if (chat.panel.hidden) chat.cerrar(); else chat.abrir();
    });

    acciones.appendChild(boton);
    info.appendChild(chat.panel);
}

// Globito con el total de mensajes sin leer, para el enlace "Mensajes" del menú
async function actualizarGloboMensajes(enlace) {
    const resultado = await pedir("/api/mensajes/pendientes");
    let globo = enlace.querySelector(".globito");
    const total = resultado.ok && Array.isArray(resultado.datos)
        ? resultado.datos.reduce(function (suma, p) { return suma + p.cantidad; }, 0) : 0;
    if (total === 0) {
        if (globo) globo.remove();
        return;
    }
    if (!globo) {
        globo = crear("span", "globito");
        enlace.appendChild(globo);
    }
    globo.textContent = String(total);
}
