# Pruebas de la API en Postman

`Nido.postman_collection.json` tiene **una carpeta por historia de usuario** (US01 a US22) con exactamente los
endpoints de la tabla "Especificación de endpoints" de cada HU en el documento. Cada petición valida el código
HTTP esperado y guarda los ids que usan las siguientes.

## Cómo ejecutarla (5 minutos)

1. **Base de datos limpia:** en MySQL Workbench ejecuta `database/bd_nido.sql`.
2. **Backend encendido:** arranca Nido (Eclipse/STS o `mvnw spring-boot:run`) y espera "Started NidoApplication".
3. **Importar:** en Postman, *Import* → arrastra `Nido.postman_collection.json`.
4. **Archivo de la foto (US12):** en Postman ve a *Settings → General → Working directory* y elige la carpeta
   `pruebas-api` (ahí está `evidencia-ejemplo.png`). Así la petición "Subir foto de evidencia" encuentra el archivo.
5. **Ejecutar todo:** clic derecho en la colección → *Run collection* → *Run*. Deben salir todas en verde.

> Importante: ejecuta la colección **sobre la base recién cargada**. Si la corres dos veces seguidas, algunas
> pruebas fallan a propósito (por ejemplo, el login `pdiaz` ya existiría). Vuelve a ejecutar `bd_nido.sql` y repite.

## Capturas para el documento
- La ventana de resultados del *Runner* con todas las pruebas en verde.
- Una petición abierta por HU (por ejemplo **US07 → CP09 Reserva solapada**, que responde `409`).

## Reporte HTML
`reporte-pruebas.html` es el reporte generado automáticamente con Newman (mismo resultado que el Runner).
Ábrelo en el navegador.

## Usuarios de prueba (contraseña `Nido2026!`)
`admin` (ADMIN), `lmendoza` y `jquispe` (PROPIETARIO), `rhuaman` y `cflores` (PERSONAL), `mtorres` (PERSONAL inactivo).
