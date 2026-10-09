# Pantalla del mockup → request de Postman

Cuando el profesor señale una pantalla (el prototipo de una HU), busca su sección, abre en Postman la carpeta con ese nombre y ejecuta el request indicado.

**Antes de empezar:** importa `Nido.postman_collection.json` y el *environment* (`Nido-local` o `Nido-AWS`), y ejecuta primero la carpeta **US01** (guarda los tokens). El orden de las carpetas importa porque algunas peticiones usan el id creado por la anterior.

**Errores que suelen pedir:** sin token → quita el header `Authorization` de cualquier `GET` y responde `401`; id inexistente → `GET /alojamientos/9999` (`404`); datos inválidos → `POST /alojamientos` sin nombre (`400`); otro propietario → `GET /alojamientos/3` con lmendoza (`403`).

## US01 Iniciar sesión

- **Pantalla:** Inicio de sesión (`/login.html`)
- **Carpeta de Postman:** «US01 Iniciar sesión»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Login ADMIN | `POST /auth/login` | 200 OK |
| Login PROPIETARIO (lmendoza) | `POST /auth/login` | 200 OK |
| Login PROPIETARIO (jquispe) | `POST /auth/login` | 200 OK |
| Login PERSONAL (rhuaman) | `POST /auth/login` | 200 OK |
| Login PERSONAL (cflores) | `POST /auth/login` | 200 OK |
| Login con contraseña incorrecta | `POST /auth/login` | 401 Unauthorized |
| Login de usuario INACTIVO | `POST /auth/login` | 401 Unauthorized |
| Perfil y menú del usuario | `GET /auth/perfil` | 200 OK |

## US02 Usuarios y roles

- **Pantalla:** Usuarios y roles (`/app/usuarios.html`)
- **Carpeta de Postman:** «US02 Usuarios y roles»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Listar usuarios | `GET /usuarios` | 200 OK |
| Registrar usuario con rol | `POST /usuarios` | 201 Created |
| Obtener usuario | `GET /usuarios/{id}` | 200 OK |
| Actualizar usuario | `PUT /usuarios/{id}` | 200 OK |
| Desactivar usuario | `PATCH /usuarios/{id}/estado` | 200 OK |
| Eliminar usuario | `DELETE /usuarios/{id}` | 204 No Content |
| Listar roles | `GET /roles` | 200 OK |
| Listar opciones de menú | `GET /opciones` | 200 OK |
| Propietario no administra usuarios | `GET /usuarios` | 403 Forbidden |

## US03 Gestión de alojamientos

- **Pantalla:** Alojamientos (listado y formulario) (`/app/alojamientos.html, /app/alojamiento-form.html`)
- **Carpeta de Postman:** «US03 Gestión de alojamientos»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Listar alojamientos del propietario | `GET /alojamientos` | 200 OK |
| Registrar alojamiento | `POST /alojamientos` | 201 Created |
| Obtener alojamiento | `GET /alojamientos/{id}` | 200 OK |
| Actualizar alojamiento | `PUT /alojamientos/{id}` | 200 OK |
| Buscar por estado (HU-01) | `GET /alojamientos/estado/DISPONIBLE` | 200 OK |
| Eliminar alojamiento | `DELETE /alojamientos/{id}` | 204 No Content |
| Ver alojamiento de otro propietario | `GET /alojamientos/3` | 403 Forbidden |
| Alojamiento inexistente | `GET /alojamientos/9999` | 404 Not Found |
| Registrar sin nombre | `POST /alojamientos` | 400 Bad Request |

## US04 Ubicación del alojamiento

- **Pantalla:** Formulario de alojamiento: ubicación en el mapa (`/app/alojamiento-form.html`)
- **Carpeta de Postman:** «US04 Ubicación del alojamiento»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Geocodificar dirección (OpenStreetMap) | `GET /geocodificacion?direccion=Malecon de la Reserva 610, Miraflores, Lima` | 200 OK |
| Confirmar ubicación | `PATCH /alojamientos/1/ubicacion` | 200 OK |
| Coordenadas fuera de rango | `PATCH /alojamientos/1/ubicacion` | 400 Bad Request |

## US05 Gestión de huéspedes

- **Pantalla:** Huéspedes (`/app/huespedes.html`)
- **Carpeta de Postman:** «US05 Gestión de huéspedes»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Listar huéspedes | `GET /huespedes` | 200 OK |
| Registrar huésped | `POST /huespedes` | 201 Created |
| Obtener huésped | `GET /huespedes/{id}` | 200 OK |
| Actualizar huésped | `PUT /huespedes/{id}` | 200 OK |
| Eliminar huésped | `DELETE /huespedes/{id}` | 204 No Content |
| Registro sin idUsuario | `POST /huespedes` | 400 Bad Request |
| Registro con idUsuario de otro usuario | `POST /huespedes` | 403 Forbidden |

## US06 Registrar reserva

- **Pantalla:** Reservas (nueva reserva) (`/app/reservas.html`)
- **Carpeta de Postman:** «US06 Registrar reserva»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Listar reservas | `GET /reservas` | 200 OK |
| Registrar reserva | `POST /reservas` | 201 Created |
| Obtener reserva | `GET /reservas/{id}` | 200 OK |
| Actualizar reserva | `PUT /reservas/{id}` | 200 OK |
| Cancelar reserva | `PATCH /reservas/{id}/cancelar` | 200 OK |
| Eliminar reserva | `DELETE /reservas/{id}` | 204 No Content |
| Salida antes de la entrada | `POST /reservas` | 400 Bad Request |
| Alojamiento INACTIVO | `POST /reservas` | 409 Conflict |

## US07 Evitar solapamiento de reservas

- **Pantalla:** Reservas: aviso de fechas ocupadas (`/app/reservas.html`)
- **Carpeta de Postman:** «US07 Evitar solapamiento de reservas»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| CP09 Reserva solapada | `POST /reservas` | 409 Conflict |
| Disponibilidad: fechas ocupadas | `GET /reservas/disponibilidad?idAlojamiento=1&entrada=2026-10-11T14:00:00&salida=2026-10-12T11:00:00` | 200 OK |
| Disponibilidad: fechas libres | `GET /reservas/disponibilidad?idAlojamiento=1&entrada=2026-12-01T14:00:00&salida=2026-12-03T11:00:00` | 200 OK |

## US08 Calendario operativo

- **Pantalla:** Calendario operativo (`/app/calendario.html`)
- **Carpeta de Postman:** «US08 Calendario operativo»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Calendario del mes | `GET /reportes/calendario?fechaInicio=2026-10-01&fechaFin=2026-10-31` | 200 OK |
| Calendario de un alojamiento | `GET /reportes/calendario?fechaInicio=2026-10-01&fechaFin=2026-10-31&idAlojamiento=1` | 200 OK |
| Reservas de un alojamiento | `GET /reservas/alojamiento/1` | 200 OK |

## US09 Gestión de personal

- **Pantalla:** Personal (`/app/personal.html`)
- **Carpeta de Postman:** «US09 Gestión de personal»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Listar personal activo | `GET /usuarios/personal?soloActivos=true` | 200 OK |
| Registrar personal | `POST /usuarios/personal` | 201 Created |
| Desactivar personal | `PATCH /usuarios/personal/{id}/estado` | 200 OK |
| Asignar servicio a personal INACTIVO | `POST /servicios` | 409 Conflict |

## US10 Crear y asignar servicio

- **Pantalla:** Servicios (crear y asignar) y Tareas móvil (`/app/servicios.html, /app/tareas.html`)
- **Carpeta de Postman:** «US10 Crear y asignar servicio»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Crear y asignar servicio | `POST /servicios` | 201 Created |
| Listar servicios | `GET /servicios` | 200 OK |
| Obtener servicio | `GET /servicios/{id}` | 200 OK |
| Mis servicios (personal) | `GET /servicios/mis-servicios` | 200 OK |
| Aceptar servicio (personal) | `PATCH /servicios/{id}/estado` | 200 OK |
| Iniciar servicio (personal) | `PATCH /servicios/{id}/estado` | 200 OK |
| Otro personal no ve el servicio | `GET /servicios/{id}` | 403 Forbidden |

## US11 Ejecutar checklist

- **Pantalla:** Tareas móvil: checklist (`/app/tareas.html`)
- **Carpeta de Postman:** «US11 Ejecutar checklist»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Completar con checklist pendiente | `PATCH /servicios/{id}/estado` | 409 Conflict |
| Marcar checklist | `PATCH /servicios/{id}/checklist` | 200 OK |
| Ítem inexistente | `PATCH /servicios/{id}/checklist` | 400 Bad Request |

## US12 Adjuntar evidencias

- **Pantalla:** Tareas móvil: fotos de evidencia (`/app/tareas.html`)
- **Carpeta de Postman:** «US12 Adjuntar evidencias»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Completar sin foto | `PATCH /servicios/{id}/estado` | 409 Conflict |
| Subir foto de evidencia | `POST /servicios/{id}/evidencias` | 201 Created |
| Listar evidencias del servicio | `GET /servicios/{id}/evidencias` | 200 OK |
| Ver foto | `GET /evidencias/{id}/archivo` | 200 OK |
| Completar servicio | `PATCH /servicios/{id}/estado` | 200 OK |

## US13 Registrar incidencias

- **Pantalla:** Incidencias (`/app/incidencias.html`)
- **Carpeta de Postman:** «US13 Registrar incidencias»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Registrar incidencia (personal) | `POST /incidencias` | 201 Created |
| Listar incidencias abiertas | `GET /incidencias?estado=ABIERTA` | 200 OK |
| Obtener incidencia | `GET /incidencias/{id}` | 200 OK |
| Actualizar incidencia | `PUT /incidencias/{id}` | 200 OK |
| Resolver incidencia | `PATCH /incidencias/{id}/estado` | 200 OK |
| Eliminar incidencia | `DELETE /incidencias/{id}` | 204 No Content |
| Prioridad inválida | `POST /incidencias` | 400 Bad Request |

## US14 Ingresos y ocupación

- **Pantalla:** Pagos e ingresos y Reportes (`/app/pagos.html, /app/reportes.html`)
- **Carpeta de Postman:** «US14 Ingresos y ocupación»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Registrar pago | `POST /pagos` | 201 Created |
| Listar pagos | `GET /pagos` | 200 OK |
| Obtener pago | `GET /pagos/{id}` | 200 OK |
| Anular pago | `PUT /pagos/{id}` | 200 OK |
| Reporte de ingresos | `GET /reportes/ingresos?fechaInicio=2026-10-01&fechaFin=2026-10-31` | 200 OK |
| Reporte de ocupación | `GET /reportes/ocupacion?fechaInicio=2026-10-01&fechaFin=2026-10-31` | 200 OK |
| Tipo de pago inválido | `POST /pagos` | 400 Bad Request |

## US15 Reporte de operación

- **Pantalla:** Reporte de operación (`/app/reportes.html`)
- **Carpeta de Postman:** «US15 Reporte de operación»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Reporte de operación (propietario) | `GET /reportes/operacion?fechaInicio=2026-10-01&fechaFin=2026-10-31` | 200 OK |
| Reporte de operación (administrador) | `GET /reportes/operacion?fechaInicio=2026-10-01&fechaFin=2026-10-31` | 200 OK |
| Periodo inválido | `GET /reportes/operacion?fechaInicio=2026-10-31&fechaFin=2026-10-01` | 400 Bad Request |

## US22 Auditoría de acciones críticas

- **Pantalla:** Auditoría (`/app/auditoria.html`)
- **Carpeta de Postman:** «US22 Auditoría de acciones críticas»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Consultar auditoría | `GET /auditorias` | 200 OK |
| Auditoría filtrada | `GET /auditorias?entidad=reservas&usuario=lmendoza` | 200 OK |
| Propietario no ve auditoría | `GET /auditorias` | 403 Forbidden |

## SOPORTE Catálogos (soporte de los formularios)

- **Pantalla:** Listas desplegables de los formularios (`(varias)`)
- **Carpeta de Postman:** «Catálogos (soporte de los formularios)»

| Request (nombre en Postman) | Método y ruta | Resultado esperado |
|---|---|---|
| Ubigeos | `GET /ubigeos` | 200 OK |
| Países | `GET /paises` | 200 OK |
| Catálogos | `GET /catalogos` | 200 OK |
| Datos de catálogo | `GET /datacatalogos` | 200 OK |
| Registrar país (admin) | `POST /paises` | 201 Created |
| Propietario no modifica catálogos | `POST /paises` | 403 Forbidden |
