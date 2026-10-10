# Bitácora del proyecto Nido: qué se hizo, por qué y cómo explicarlo

Esta guía resume todo lo construido para el Trabajo Parcial. Léela antes de la exposición: la rúbrica da
**3 puntos** a responder bien (y en menos de 1 minuto) las preguntas del profesor. Cada sección termina con
"**Cómo explicarlo**", que es lo que puedes decir en voz alta.

---

## 1. Arquitectura general

```
Landing Page (GitHub Pages)  ─┐
                              ├──HTTP──▶  Spring Boot (API REST /api/v1 + aplicación web)  ──JPA──▶  MySQL bd_nido
Aplicación web (HTML/CSS/JS) ─┘                │
                                               └──HTTP──▶ OpenStreetMap Nominatim (geocodificación, US04)
```

- **Backend:** Spring Boot 4, Java 25, Maven. Paquete `com.hotel` organizado **por capas**:
  `entity` (tablas) → `repository` (consultas) → `service` + `service/impl` (reglas de negocio) → `controller` (endpoints).
- **Base de datos:** MySQL 8, esquema creado por el script `database/bd_nido.sql` (`ddl-auto=none`: Hibernate no toca las tablas).
- **Frontend:** páginas en `src/main/resources/static` servidas por el mismo Spring Boot. Llaman a la API con `fetch`.
- **Landing:** repositorio aparte `nido-landing` (la rúbrica pide un repositorio por producto).

**Cómo explicarlo:** "Usamos una arquitectura en capas. El controlador recibe la petición HTTP, el servicio
aplica las reglas de negocio y el repositorio habla con MySQL usando JPA. Cada capa tiene una sola
responsabilidad, así un cambio en la base de datos no afecta a los controladores."

---

## 2. Seguridad: JWT y roles (US01, US02)

**Flujo de login:**
1. `POST /api/v1/auth/login` con `{login, password}`.
2. Spring Security compara la contraseña con el hash **BCrypt** de la tabla `usuario` (`UsuarioDetailsService`).
3. Si es correcta, `JwtService` genera un **token JWT** firmado (HS256, 120 minutos) con el login y los roles.
4. El cliente envía `Authorization: Bearer <token>` en cada petición; `JwtAuthenticationFilter` lo valida.

**Roles:** ADMIN, PROPIETARIO y PERSONAL (tablas `rol`, `usuario_has_rol`). Las reglas de acceso por URL están en
`SecurityConfig` (por ejemplo, `/api/v1/usuarios/**` solo ADMIN). El menú de cada rol sale de `rol_has_opcion`.

**Detalles que suman:**
- El login falla con un **mensaje genérico** ("Usuario o contraseña incorrectos") para no revelar qué dato falló.
- Un usuario **INACTIVO** no puede iniciar sesión (`disabled` en `UsuarioDetailsService`).
- La contraseña **nunca** se devuelve en el JSON (`@JsonProperty(access = WRITE_ONLY)`).

**Cómo explicarlo:** "Usamos JWT porque la API es *stateless*: el servidor no guarda sesiones, el token lleva
quién es el usuario y sus roles, firmado con una clave secreta. Las contraseñas se guardan con BCrypt, que es un
hash con sal, así ni siquiera nosotros podemos ver la contraseña original."

---

## 3. Reglas de negocio (dónde está cada una)

| HU | Regla | Dónde está el código |
|---|---|---|
| US03 | Un propietario solo ve y opera **sus** alojamientos (y sus reservas, pagos, servicios, incidencias y reportes) | `UsuarioActual.puedeVer()` / `verificarAlojamiento()`, usado en cada `ServiceImpl` |
| US03, US05 | No se registran duplicados: dirección única (y nombre único por propietario) en alojamientos; correo único (o nombres + apellidos + teléfono) en huéspedes | `AlojamientoServiceImpl.validarSinDuplicados()` y `HuespedServiceImpl.validarSinDuplicados()` → 409 |
| US07 | Dos reservas activas del mismo alojamiento no se pueden cruzar | `ReservaRepository.buscarSolapadas()` + `ReservaServiceImpl.prepararYValidar()` |
| US06 | Si no se envía precio: noches × precio base; salida posterior a la entrada; alojamiento INACTIVO no reserva | `ReservaServiceImpl` |
| US09 | Solo se asigna personal ACTIVO con rol PERSONAL | `ServicioServiceImpl.prepararYValidar()` |
| US10 | Ciclo del servicio ASIGNADO → ACEPTADO/RECHAZADO → EN_PROCESO → COMPLETADO | Mapa `TRANSICIONES` en `ServicioServiceImpl` |
| US11 | No se completa con ítems obligatorios del checklist pendientes | `ServicioServiceImpl.validarCierre()` |
| US12 | No se completa sin al menos una foto; solo JPG/PNG/WEBP de hasta 5 MB | `validarCierre()` y `EvidenciaServiceImpl.subir()` |
| US22 | Toda creación, edición o eliminación queda en la tabla `auditoria` | `AuditoriaInterceptor` (se ejecuta después de cada petición) |
| — | `idUsuario` (quién registra) es obligatorio y debe ser el usuario del token | `UsuarioActual.verificarRegistrante()` |

**La consulta de solapamiento (US07)**, la más probable de que te pregunten:
```sql
r.entrada < :salida AND r.salida > :entrada   -- y que no esté CANCELADA
```
Dos intervalos se cruzan si uno empieza antes de que el otro termine. Por eso una reserva que **sale** el 13/10 a
las 11:00 y otra que **entra** el 13/10 a las 14:00 sí se permiten.

**Cómo explicarlo:** "Las reglas viven en la capa de servicio, no en el controlador ni en el frontend, porque así se
aplican siempre, venga la petición de la web, de Postman o de otra app."

---

## 4. Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) convierte las excepciones en JSON con el código correcto:

| Código | Cuándo |
|---|---|
| 400 Bad Request | Datos inválidos (`@Valid`), fechas al revés, estado o prioridad inválida, falta `idUsuario` |
| 401 Unauthorized | Sin token o token vencido; login incorrecto |
| 403 Forbidden | El rol no tiene permiso o el dato es de otro propietario |
| 404 Not Found | El id no existe |
| 409 Conflict | Regla de negocio violada (reserva solapada, servicio sin checklist, registro con datos relacionados) |

Ejemplo: `{"status":409,"error":"Conflict","mensaje":"El alojamiento ya esta reservado del ...","ruta":"/api/v1/reservas"}`

---

## 5. Base de datos

- 17 tablas; diagrama en `database/diagrama-bd_nido.png` (generado desde el script con Graphviz).
- **Al menos 10 registros por tabla**, coherentes entre sí (verificado: sin reservas solapadas, servicios
  completados con checklist y foto, reservas e incidencias del mismo alojamiento).
- Contraseña de todos los usuarios de prueba: `Nido2026!` (guardada como hash BCrypt).
- **`idUsuario`** en alojamiento, huésped, reserva, pago, servicio, incidencia y evidencia = quién registró el dato.
  En `servicio` el personal asignado está en `idPersonal`.

**Cómo explicarlo:** "Separamos `idPersonal` (quién hace el servicio) de `idUsuario` (quién lo registró) para tener
trazabilidad completa, como pidió el profesor."

---

## 6. API externa (US04)

`GET /api/v1/geocodificacion?direccion=...` consulta **OpenStreetMap Nominatim** (gratuita, sin clave) desde el
backend con `java.net.http.HttpClient` y devuelve latitud, longitud y la dirección encontrada. El propietario las
confirma con `PATCH /api/v1/alojamientos/{id}/ubicacion`. Si el servicio externo falla, la API responde 503 y el
usuario puede escribir las coordenadas a mano.

**Cómo explicarlo:** "La llamada a la API externa la hace el backend, no el navegador, así controlamos el
*User-Agent* que exige OpenStreetMap y manejamos los errores en un solo lugar."

---

## 7. Pruebas

| Tipo | Dónde | Resultado |
|---|---|---|
| Unitarias (JUnit 5 + Mockito) | `src/test/java/com/hotel/...` | 24 pruebas: duplicados, solapamiento, fechas, precio, ciclo del servicio, checklist, evidencia, personal inactivo, idUsuario |
| API (Postman) | `pruebas-api/Nido.postman_collection.json` | 95 peticiones en 17 carpetas (una por HU), 126 validaciones |
| Reporte | `pruebas-api/reporte-pruebas.html` | Generado con Newman |

**Cómo explicarlo:** "Las pruebas unitarias usan Mockito para simular los repositorios, así probamos la regla de
negocio sin base de datos. Postman prueba la API completa de punta a punta."

---

## 8. Frontend

- HTML, CSS y JavaScript sin frameworks (Angular queda para el Sprint 3).
- `css/nido.css` aplica la guía de estilo de Figma: azul petróleo `#0F4C5C`, ámbar `#E09F3E`, Poppins e Inter,
  espaciado de 8 px, radios de 8/12/16 px, breakpoints 1200/1024/768/480.
- `js/nido.js`: guarda el token, llama a la API, arma el menú según las opciones del rol, muestra toasts de 4 s y
  confirmaciones antes de eliminar.
- **Los formularios envían exactamente los campos del JSON del endpoint** (requisito del profesor).
- Vista móvil para el personal (`tareas.html`): checklist táctil, cámara para fotos y barra inferior.

---

## 9. Comandos útiles

```bash
# Cargar la base de datos
mysql -u root -p < database/bd_nido.sql
# Ejecutar el backend
./mvnw spring-boot:run
# Ejecutar las pruebas unitarias (con MySQL encendido)
./mvnw test
# Ejecutar la colección de Postman por consola
npx newman run pruebas-api/Nido.postman_collection.json --working-dir pruebas-api
```

---

## 10. Preguntas probables del profesor (respuesta en menos de 1 minuto)

1. **¿Qué es JWT y por qué lo usan?** Token firmado con los datos del usuario; la API no guarda sesión (*stateless*) y escala mejor.
2. **¿Cómo evitan dobles reservas?** Consulta `entrada < salidaNueva AND salida > entradaNueva` sobre reservas no canceladas del mismo alojamiento; si hay alguna, 409.
3. **¿Cómo un propietario no ve datos de otro?** Cada servicio verifica que el alojamiento pertenezca al usuario del token (`UsuarioActual`); si no, 403.
4. **¿Qué diferencia hay entre `@Controller` y `@RestController`?** `@RestController` devuelve datos (JSON) en vez de vistas.
5. **¿Para qué sirve `@Transactional`?** Agrupa las operaciones en una transacción: si algo falla, se revierte todo.
6. **¿Qué hace `@Valid`?** Activa las validaciones de la entidad (`@NotBlank`, `@Positive`...) y si fallan responde 400.
7. **¿Por qué `ddl-auto=none`?** El esquema lo controla el script SQL; Hibernate no debe crear ni alterar tablas.
8. **¿Cómo guardan las contraseñas?** Con BCrypt (hash con sal); nunca en texto plano ni en las respuestas.
9. **¿Qué es la auditoría?** Un interceptor registra usuario, fecha, entidad y operación de cada POST/PUT/PATCH/DELETE exitoso.
10. **¿Cómo se despliega?** Jar en una EC2 con Java 25 y MySQL en RDS; credenciales por variables de entorno (ver `docs/DESPLIEGUE-AWS.md`).

---

## 11. Registro de cambios (en orden)

| # | Qué se hizo | Por qué |
|---|---|---|
| 1 | Contraseña de MySQL fuera de Git, sin `target/` | No subir secretos ni archivos compilados |
| 2 | Integración de HU-01 (Oscar) y HU15 (Fabricio) | Unir el trabajo del equipo |
| 3 | Script SQL limpio y datos de prueba | Las columnas duplicadas rompían la aplicación |
| 4 | Seguridad JWT, roles, auditoría, Swagger y errores en JSON | US01, US02, US22 |
| 5 | CRUD de catálogos, usuarios y personal | US02, US09 |
| 6 | Reglas de negocio, servicios, checklist, evidencias y reportes | US03, US06-US15 |
| 7 | Zona horaria fija en Lima | En AWS (UTC) las horas salían corridas 5 horas |
| 8 | `idUsuario` en registros y 10+ registros por tabla | Pedido del profesor |
| 9 | US04 con OpenStreetMap y pruebas JUnit | API externa y calidad |
| 10 | Frontend real con la guía de Figma | Prototipos reales por HU |
| 11 | Landing en repositorio propio | Rúbrica: repositorio por producto y landing publicada |
| 12 | Postman por HU y reporte HTML | Pedido del profesor |
