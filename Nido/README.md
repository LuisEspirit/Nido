# Nido – API REST de gestión de alojamientos temporales

Backend en Spring Boot 4 (Java 25) + MySQL 8, con seguridad JWT por roles.

## Cómo ejecutarlo en local

1. **Base de datos**: en MySQL Workbench abra y ejecute `database/bd_nido.sql`
   (o `mysql -u root -p < database/bd_nido.sql`). Borra y vuelve a crear `bd_nido` con datos de prueba.
2. **Contraseña de MySQL**: cree `src/main/resources/application-local.properties` (no se sube a GitHub):
   ```properties
   spring.datasource.password=SU_CONTRASENA_DE_MYSQL
   ```
3. **Arrancar**: desde Eclipse/STS (Run As > Spring Boot App) o con `./mvnw spring-boot:run`.
4. **Probar**: Swagger en <http://localhost:8080/swagger-ui.html>, o importe en Postman
   `pruebas-api/Nido.postman_collection.json` y ejecute la colección completa con *Runner*.

## Usuarios de prueba (contraseña `Nido2026!`)

| login      | rol         | notas                                  |
|------------|-------------|----------------------------------------|
| `admin`    | ADMIN       | ve y administra todo                   |
| `lmendoza` | PROPIETARIO | alojamientos 1, 2 y 5                  |
| `jquispe`  | PROPIETARIO | alojamientos 3 y 4                     |
| `rhuaman`  | PERSONAL    | limpieza                               |
| `cflores`  | PERSONAL    | mantenimiento                          |
| `mtorres`  | PERSONAL    | INACTIVO: no puede iniciar sesión      |

Primero `POST /api/v1/auth/login` con `{"login":"admin","password":"Nido2026!"}`; luego envíe el token en el
header `Authorization: Bearer <token>`.

## Endpoints principales (`/api/v1`)

| Módulo | Endpoints | Roles |
|---|---|---|
| Autenticación (US01) | `POST /auth/login`, `GET /auth/perfil` | público / autenticado |
| Alojamientos (HU-01, US03) | CRUD `/alojamientos`, `GET /alojamientos/estado/{estado}` | ADMIN, PROPIETARIO |
| Huéspedes | CRUD `/huespedes` | ADMIN, PROPIETARIO |
| Reservas (US07) | CRUD `/reservas`, `GET /reservas/alojamiento/{id}`, `GET /reservas/disponibilidad`, `PATCH /reservas/{id}/cancelar` | ADMIN, PROPIETARIO |
| Pagos | CRUD `/pagos` | ADMIN, PROPIETARIO |
| Servicios (US10, US11) | CRUD `/servicios`, `GET /servicios/mis-servicios`, `PATCH /servicios/{id}/estado`, `PATCH /servicios/{id}/checklist` | gestión: ADMIN, PROPIETARIO; ejecución: PERSONAL |
| Evidencias (US12) | `POST/GET /servicios/{id}/evidencias`, `GET /evidencias/{id}/archivo`, `DELETE /evidencias/{id}` | ADMIN, PROPIETARIO, PERSONAL |
| Incidencias | CRUD `/incidencias` (filtros `estado`, `prioridad`, `idAlojamiento`), `PATCH /incidencias/{id}/estado` | reportar: todos; gestionar: ADMIN, PROPIETARIO |
| Reportes (US08, US14, US15) | `GET /reportes/ingresos`, `/reportes/ocupacion`, `/reportes/calendario`, `/reportes/operacion` | ADMIN, PROPIETARIO |
| Personal (US09) | `GET/POST /usuarios/personal`, `PATCH /usuarios/personal/{id}/estado` | ADMIN, PROPIETARIO |
| Usuarios y roles (US02) | CRUD `/usuarios`, `PATCH /usuarios/{id}/estado`, CRUD `/roles`, `/opciones` | ADMIN |
| Catálogos | CRUD `/ubigeos`, `/paises`, `/catalogos`, `/datacatalogos` | consulta: autenticado; cambios: ADMIN |
| Auditoría (US22) | `GET /auditorias?entidad=&usuario=` | ADMIN |

## Reglas de negocio implementadas

- **US03** – Un propietario solo ve y opera sus propios alojamientos y lo relacionado (reservas, pagos,
  servicios, incidencias, reportes). Si lo intenta con uno ajeno recibe `403`.
- **US07** – Una reserva no puede cruzarse con otra activa del mismo alojamiento (`409`, caso CP09).
  La salida debe ser posterior a la entrada y un alojamiento `INACTIVO` no acepta reservas.
  Si no se envía precio se calcula como noches × precio base. Cancelar libera las fechas.
- **US09** – Solo se asignan servicios a personal `ACTIVO`; un usuario `INACTIVO` no puede iniciar sesión.
- **US10** – Ciclo del servicio: `ASIGNADO → ACEPTADO/RECHAZADO → EN_PROCESO → COMPLETADO` (o `CANCELADO`).
  El personal solo opera los servicios que tiene asignados.
- **US11** – No se puede completar un servicio con ítems obligatorios del checklist sin marcar.
- **US12** – No se puede completar un servicio sin al menos una foto (JPG, PNG o WEBP, máximo 5 MB).
- **US22** – Toda creación, modificación o eliminación exitosa queda registrada en la tabla `auditoria`.

Los errores se devuelven en JSON: `{"status": 409, "error": "Conflict", "mensaje": "...", "ruta": "..."}`.

## Despliegue

Ver [docs/DESPLIEGUE-AWS.md](docs/DESPLIEGUE-AWS.md).
