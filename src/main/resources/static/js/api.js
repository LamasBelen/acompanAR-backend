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
                { texto: "Mis capacitaciones", url: "mis-capacitaciones.html" }
            ],
            EMPLEADOR: [
                { texto: "Mis ofertas", url: "mis-ofertas.html" },
                { texto: "Publicar oferta", url: "oferta-form.html" }
            ],
            ADMINISTRADOR: [
                { texto: "Capacitaciones", url: "admin-capacitaciones.html" },
                { texto: "Nueva capacitación", url: "capacitacion-form.html" }
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

// Convierte una fecha "2026-10-28" en "28/10/2026"
function formatearFecha(fecha) {
    if (!fecha) return "";
    const partes = fecha.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}