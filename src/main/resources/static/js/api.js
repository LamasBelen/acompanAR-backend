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
async function armarMenu() {
    const menu = document.getElementById("menu");
    if (!menu) return;

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
                { texto: "Capacitaciones", url: "capacitaciones.html" },
                { texto: "Mis capacitaciones", url: "mis-capacitaciones.html" },
                { texto: "Mi perfil", url: "perfil.html" }
            ],
            EMPLEADOR: [
                { texto: "Mis ofertas", url: "mis-ofertas.html" },
                { texto: "Publicar oferta", url: "oferta-form.html" },
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
                { texto: "Instituciones y tipos", url: "admin-catalogos.html" }
            ]
        };
        const enlaces = enlacesPorRol[usuario.rol] || [];
        enlaces.forEach(function (e) {
            menu.appendChild(crear("a", "", e.texto, { href: e.url }));
        });

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
            "★ " + c.puntaje + " · " + c.nombreAutor + " · " + c.tituloOferta + " · " + formatearFecha(c.fecha)));
        if (c.comentario) bloque.appendChild(crear("p", "", c.comentario));
        contenedor.appendChild(bloque);
    });
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
    const boton = crear("button", "boton boton-chico boton-secundario", "Denunciar");
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
