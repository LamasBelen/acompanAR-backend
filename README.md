# acompanAR

Plataforma para conectar enfermeros/as y acompañantes terapéuticos con empleadores en Río Grande,
y publicar capacitaciones de instituciones públicas y privadas.

## Variables de entorno (credenciales)

Ninguna credencial está escrita en el código ni en `application*.properties`:
en el repositorio solo figura el **nombre** de cada variable. Los valores se cargan en tu computadora
(en IntelliJ: *Run → Edit Configurations → Environment variables*, separadas por `;`).

| Variable | Para qué sirve |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` (por defecto) o `remoto` |
| `DB_LOCAL_URL` | URL de la base local, ej. `jdbc:postgresql://localhost:5432/acompanardb` |
| `DB_LOCAL_USUARIO` | Usuario de la base local |
| `DB_LOCAL_CONTRASENA` | Contraseña de la base local |
| `DB_REMOTO_URL` | URL de la base remota (solo con el perfil `remoto`) |
| `DB_REMOTO_USUARIO` | Usuario de la base remota |
| `DB_REMOTO_CONTRASENA` | Contraseña de la base remota |
| `ADMIN_USUARIO` | Usuario del administrador inicial |
| `ADMIN_CONTRASENA` | Contraseña del administrador inicial |

Si falta una variable, la aplicación no arranca y el error dice cuál es.
