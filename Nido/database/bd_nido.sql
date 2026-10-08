-- =====================================================================
--  Nido - Gestion inteligente de alojamientos temporales
--  Script de base de datos bd_nido (MySQL 8 o superior)
--
--  Crea el esquema completo y carga datos de prueba.
--  Ejecutar en MySQL Workbench (File > Run SQL Script) o con:
--      mysql -u root -p < database/bd_nido.sql
--
--  ATENCION: borra y vuelve a crear la base bd_nido.
--
--  Usuarios de prueba (todos con la contrasena de desarrollo: Nido2026!)
--  La contrasena se guarda cifrada con BCrypt en la columna password.
--      login      rol           estado
--      admin      ADMIN         ACTIVO
--      lmendoza   PROPIETARIO   ACTIVO
--      jquispe    PROPIETARIO   ACTIVO
--      rhuaman    PERSONAL      ACTIVO   (limpieza)
--      cflores    PERSONAL      ACTIVO   (mantenimiento)
--      mtorres    PERSONAL      INACTIVO (no puede iniciar sesion)
-- =====================================================================

DROP DATABASE IF EXISTS bd_nido;
CREATE DATABASE bd_nido DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE bd_nido;

-- ---------------------------------------------------------------------
--  Catalogos y ubicacion
-- ---------------------------------------------------------------------
CREATE TABLE pais (
  idPais  INT NOT NULL AUTO_INCREMENT,
  iso     CHAR(2)     DEFAULT NULL,
  nombre  VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (idPais)
) ENGINE=InnoDB;

CREATE TABLE ubigeo (
  idubigeo     INT NOT NULL AUTO_INCREMENT,
  departamento VARCHAR(45) DEFAULT NULL,
  provincia    VARCHAR(45) DEFAULT NULL,
  distrito     VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (idubigeo)
) ENGINE=InnoDB;

CREATE TABLE catalogo (
  idCatalogo  INT NOT NULL AUTO_INCREMENT,
  descripcion TEXT,
  estado      VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (idCatalogo)
) ENGINE=InnoDB;

CREATE TABLE datacatalogo (
  idDataCatalogo INT NOT NULL AUTO_INCREMENT,
  descripcion    TEXT,
  estado         VARCHAR(45) DEFAULT NULL,
  idCatalogo     INT NOT NULL,
  PRIMARY KEY (idDataCatalogo),
  KEY fk_datacatalogo_catalogo (idCatalogo),
  CONSTRAINT fk_datacatalogo_catalogo FOREIGN KEY (idCatalogo) REFERENCES catalogo (idCatalogo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Seguridad: usuarios, roles y opciones (US01, US02, US09)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  idusuario       INT NOT NULL AUTO_INCREMENT,
  nombres         VARCHAR(100) DEFAULT NULL,
  apellidos       VARCHAR(100) DEFAULT NULL,
  dni             VARCHAR(8)   DEFAULT NULL,
  login           VARCHAR(15)  NOT NULL,
  password        VARCHAR(200) NOT NULL,
  correo          VARCHAR(45)  DEFAULT NULL,
  fechaRegistro   DATETIME     DEFAULT NULL,
  fechaNacimiento DATE         DEFAULT NULL,
  direccion       TEXT,
  especialidad    VARCHAR(45)  DEFAULT NULL,
  estado          VARCHAR(45)  NOT NULL DEFAULT 'ACTIVO',
  idubigeo        INT DEFAULT NULL,
  PRIMARY KEY (idusuario),
  UNIQUE KEY uq_usuario_login (login),
  KEY fk_usuario_ubigeo (idubigeo),
  CONSTRAINT fk_usuario_ubigeo FOREIGN KEY (idubigeo) REFERENCES ubigeo (idubigeo)
) ENGINE=InnoDB;

CREATE TABLE rol (
  idrol  INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(45) DEFAULT NULL,
  estado VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (idrol)
) ENGINE=InnoDB;

CREATE TABLE opcion (
  idopcion INT NOT NULL AUTO_INCREMENT,
  nombre   VARCHAR(45) DEFAULT NULL,
  estado   VARCHAR(45) DEFAULT NULL,
  ruta     TEXT,
  tipo     SMALLINT DEFAULT NULL,
  PRIMARY KEY (idopcion)
) ENGINE=InnoDB;

CREATE TABLE usuario_has_rol (
  idusuario INT NOT NULL,
  idrol     INT NOT NULL,
  PRIMARY KEY (idusuario, idrol),
  KEY fk_uhr_rol (idrol),
  CONSTRAINT fk_uhr_rol     FOREIGN KEY (idrol)     REFERENCES rol (idrol),
  CONSTRAINT fk_uhr_usuario FOREIGN KEY (idusuario) REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE rol_has_opcion (
  idrol    INT NOT NULL,
  idopcion INT NOT NULL,
  PRIMARY KEY (idrol, idopcion),
  KEY fk_rho_opcion (idopcion),
  CONSTRAINT fk_rho_opcion FOREIGN KEY (idopcion) REFERENCES opcion (idopcion),
  CONSTRAINT fk_rho_rol    FOREIGN KEY (idrol)    REFERENCES rol (idrol)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Portafolio, huespedes, reservas y pagos (US03 - US08, US14)
-- ---------------------------------------------------------------------
CREATE TABLE alojamiento (
  idAlojamiento INT NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(100) DEFAULT NULL,
  direccion     TEXT,
  latitud       DOUBLE DEFAULT NULL,
  longitud      DOUBLE DEFAULT NULL,
  capacidad     INT    DEFAULT NULL,
  precioBase    DOUBLE DEFAULT NULL,
  estado        VARCHAR(45) DEFAULT NULL,
  idPropietario INT NOT NULL,
  idubigeo      INT DEFAULT NULL,
  PRIMARY KEY (idAlojamiento),
  KEY fk_alojamiento_usuario (idPropietario),
  KEY fk_alojamiento_ubigeo (idubigeo),
  CONSTRAINT fk_alojamiento_ubigeo  FOREIGN KEY (idubigeo)      REFERENCES ubigeo (idubigeo),
  CONSTRAINT fk_alojamiento_usuario FOREIGN KEY (idPropietario) REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE huesped (
  idHuesped      INT NOT NULL AUTO_INCREMENT,
  nombres        VARCHAR(100) DEFAULT NULL,
  apellidos      VARCHAR(100) DEFAULT NULL,
  correo         VARCHAR(100) DEFAULT NULL,
  telefono       VARCHAR(45)  DEFAULT NULL,
  consentimiento TINYINT(1)   DEFAULT NULL,
  PRIMARY KEY (idHuesped)
) ENGINE=InnoDB;

CREATE TABLE reserva (
  idReserva     INT NOT NULL AUTO_INCREMENT,
  entrada       DATETIME    DEFAULT NULL,
  salida        DATETIME    DEFAULT NULL,
  canal         VARCHAR(45) DEFAULT NULL,
  precio        DOUBLE      DEFAULT NULL,
  moneda        VARCHAR(10) DEFAULT NULL,
  estado        VARCHAR(45) DEFAULT NULL,
  idAlojamiento INT NOT NULL,
  idHuesped     INT NOT NULL,
  PRIMARY KEY (idReserva),
  KEY fk_reserva_alojamiento (idAlojamiento),
  KEY fk_reserva_huesped (idHuesped),
  CONSTRAINT fk_reserva_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_reserva_huesped     FOREIGN KEY (idHuesped)     REFERENCES huesped (idHuesped)
) ENGINE=InnoDB;

CREATE TABLE pago (
  idPago    INT NOT NULL AUTO_INCREMENT,
  tipo      VARCHAR(45) DEFAULT NULL,
  importe   DOUBLE      DEFAULT NULL,
  moneda    VARCHAR(10) DEFAULT NULL,
  fecha     DATETIME    DEFAULT NULL,
  estado    VARCHAR(45) DEFAULT NULL,
  idReserva INT NOT NULL,
  PRIMARY KEY (idPago),
  KEY fk_pago_reserva (idReserva),
  CONSTRAINT fk_pago_reserva FOREIGN KEY (idReserva) REFERENCES reserva (idReserva)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Operacion: servicios, evidencias e incidencias (US10 - US13, US15)
-- ---------------------------------------------------------------------
CREATE TABLE servicio (
  idServicio    INT NOT NULL AUTO_INCREMENT,
  tipo          VARCHAR(45) DEFAULT NULL,
  inicio        DATETIME    DEFAULT NULL,
  fin           DATETIME    DEFAULT NULL,
  estado        VARCHAR(45) DEFAULT NULL,
  checklist     TEXT,
  idAlojamiento INT NOT NULL,
  idReserva     INT DEFAULT NULL,
  idUsuario     INT NOT NULL,
  PRIMARY KEY (idServicio),
  KEY fk_servicio_alojamiento (idAlojamiento),
  KEY fk_servicio_reserva (idReserva),
  KEY fk_servicio_usuario (idUsuario),
  CONSTRAINT fk_servicio_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_servicio_reserva     FOREIGN KEY (idReserva)     REFERENCES reserva (idReserva),
  CONSTRAINT fk_servicio_usuario     FOREIGN KEY (idUsuario)     REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE evidencia (
  idEvidencia   INT NOT NULL AUTO_INCREMENT,
  nombreArchivo VARCHAR(200) DEFAULT NULL,
  ruta          TEXT,
  tipoArchivo   VARCHAR(45)  DEFAULT NULL,
  tamanio       BIGINT       DEFAULT NULL,
  fecha         DATETIME     DEFAULT NULL,
  idServicio    INT NOT NULL,
  idUsuario     INT DEFAULT NULL,
  PRIMARY KEY (idEvidencia),
  KEY fk_evidencia_servicio (idServicio),
  KEY fk_evidencia_usuario (idUsuario),
  CONSTRAINT fk_evidencia_servicio FOREIGN KEY (idServicio) REFERENCES servicio (idServicio),
  CONSTRAINT fk_evidencia_usuario  FOREIGN KEY (idUsuario)  REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE incidencia (
  idIncidencia  INT NOT NULL AUTO_INCREMENT,
  categoria     VARCHAR(45) DEFAULT NULL,
  prioridad     VARCHAR(45) DEFAULT NULL,
  descripcion   TEXT,
  estado        VARCHAR(45) DEFAULT NULL,
  idAlojamiento INT NOT NULL,
  idReserva     INT DEFAULT NULL,
  idServicio    INT DEFAULT NULL,
  PRIMARY KEY (idIncidencia),
  KEY fk_incidencia_alojamiento (idAlojamiento),
  KEY fk_incidencia_reserva (idReserva),
  KEY fk_incidencia_servicio (idServicio),
  CONSTRAINT fk_incidencia_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_incidencia_reserva     FOREIGN KEY (idReserva)     REFERENCES reserva (idReserva),
  CONSTRAINT fk_incidencia_servicio    FOREIGN KEY (idServicio)    REFERENCES servicio (idServicio)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Auditoria de acciones criticas (US22)
-- ---------------------------------------------------------------------
CREATE TABLE auditoria (
  idAuditoria INT NOT NULL AUTO_INCREMENT,
  usuario     VARCHAR(45) DEFAULT NULL,
  fecha       DATETIME    DEFAULT NULL,
  entidad     VARCHAR(45) DEFAULT NULL,
  operacion   VARCHAR(45) DEFAULT NULL,
  idRegistro  VARCHAR(45) DEFAULT NULL,
  detalle     TEXT,
  PRIMARY KEY (idAuditoria)
) ENGINE=InnoDB;

-- =====================================================================
--  DATOS DE PRUEBA
-- =====================================================================

INSERT INTO pais (iso, nombre) VALUES
 ('PE','Peru'), ('AR','Argentina'), ('BR','Brasil'), ('CL','Chile'),
 ('CO','Colombia'), ('MX','Mexico'), ('ES','Espana'), ('US','Estados Unidos');

INSERT INTO ubigeo (departamento, provincia, distrito) VALUES
 ('Lima','Lima','Miraflores'),
 ('Lima','Lima','Barranco'),
 ('Lima','Lima','San Isidro'),
 ('Cusco','Cusco','Cusco'),
 ('Arequipa','Arequipa','Yanahuara');

INSERT INTO catalogo (descripcion, estado) VALUES
 ('Tipo de servicio','ACTIVO'),
 ('Categoria de incidencia','ACTIVO'),
 ('Canal de reserva','ACTIVO'),
 ('Moneda','ACTIVO');

INSERT INTO datacatalogo (descripcion, estado, idCatalogo) VALUES
 ('LIMPIEZA','ACTIVO',1), ('MANTENIMIENTO','ACTIVO',1), ('INSPECCION','ACTIVO',1),
 ('LIMPIEZA','ACTIVO',2), ('MANTENIMIENTO','ACTIVO',2), ('DANO','ACTIVO',2), ('SEGURIDAD','ACTIVO',2), ('OTRO','ACTIVO',2),
 ('AIRBNB','ACTIVO',3), ('BOOKING','ACTIVO',3), ('DIRECTO','ACTIVO',3), ('WEB','ACTIVO',3),
 ('PEN','ACTIVO',4), ('USD','ACTIVO',4);

INSERT INTO rol (nombre, estado) VALUES
 ('ADMIN','ACTIVO'),
 ('PROPIETARIO','ACTIVO'),
 ('PERSONAL','ACTIVO');

INSERT INTO opcion (nombre, estado, ruta, tipo) VALUES
 ('Panel','ACTIVO','/panel',1),
 ('Alojamientos','ACTIVO','/alojamientos',1),
 ('Reservas','ACTIVO','/reservas',1),
 ('Huespedes','ACTIVO','/huespedes',1),
 ('Servicios','ACTIVO','/servicios',1),
 ('Incidencias','ACTIVO','/incidencias',1),
 ('Pagos e ingresos','ACTIVO','/pagos',1),
 ('Reportes','ACTIVO','/reportes',1),
 ('Usuarios y roles','ACTIVO','/usuarios',2),
 ('Auditoria','ACTIVO','/auditoria',2),
 ('Mis tareas','ACTIVO','/mis-tareas',3);

INSERT INTO rol_has_opcion (idrol, idopcion) VALUES
 (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
 (2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),
 (3,6),(3,11);

-- Contrasena de todos los usuarios de prueba: Nido2026!  (hash BCrypt)
INSERT INTO usuario (nombres, apellidos, dni, login, password, correo, fechaRegistro, fechaNacimiento, direccion, especialidad, estado, idubigeo) VALUES
 ('Administrador','Nido','70000001','admin',   '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','admin@nido.pe',     '2026-08-01 09:00:00','1990-01-15','Av. Larco 100',          NULL,           'ACTIVO',  1),
 ('Lucia','Mendoza Rojas','70000002','lmendoza','$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','lucia@nido.pe',     '2026-08-02 10:00:00','1987-04-22','Calle Schell 250',       NULL,           'ACTIVO',  1),
 ('Jorge','Quispe Huaman','70000003','jquispe', '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','jorge@nido.pe',     '2026-08-03 11:00:00','1982-11-03','Av. El Sol 320',         NULL,           'ACTIVO',  4),
 ('Rosa','Huaman Castro','70000004','rhuaman', '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','rosa@nido.pe',      '2026-08-05 08:30:00','1994-06-10','Jr. Union 45',           'LIMPIEZA',     'ACTIVO',  2),
 ('Carlos','Flores Diaz','70000005','cflores', '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','carlos@nido.pe',    '2026-08-06 08:30:00','1989-02-28','Av. Grau 780',           'MANTENIMIENTO','ACTIVO',  2),
 ('Maria','Torres Vega','70000006','mtorres',  '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','maria@nido.pe',     '2026-08-07 08:30:00','1996-09-14','Calle Lima 12',          'LIMPIEZA',     'INACTIVO',3);

INSERT INTO usuario_has_rol (idusuario, idrol) VALUES
 (1,1), (2,2), (3,2), (4,3), (5,3), (6,3);

INSERT INTO alojamiento (nombre, direccion, latitud, longitud, capacidad, precioBase, estado, idPropietario, idubigeo) VALUES
 ('Departamento Miraflores Vista Mar','Malecon de la Reserva 610, Miraflores', -12.1318, -77.0300, 4, 180.0, 'DISPONIBLE', 2, 1),
 ('Loft Barranco Bohemio',            'Jr. Batallon Ayacucho 271, Barranco',  -12.1490, -77.0215, 2, 140.0, 'DISPONIBLE', 2, 2),
 ('Suite Ejecutiva San Isidro',       'Av. Camino Real 390, San Isidro',      -12.0975, -77.0365, 3, 220.0, 'DISPONIBLE', 3, 3),
 ('Casa Andina Centro Cusco',         'Calle Saphi 840, Cusco',               -13.5150, -71.9820, 6, 260.0, 'DISPONIBLE', 3, 4),
 ('Studio Yanahuara',                 'Calle Jerusalen 105, Yanahuara',       -16.3890, -71.5450, 2, 110.0, 'INACTIVO',   2, 5);

INSERT INTO huesped (nombres, apellidos, correo, telefono, consentimiento) VALUES
 ('Ana','Garcia Lopez','ana.garcia@mail.com','+51 987654321',1),
 ('John','Smith','john.smith@mail.com','+1 2025550143',1),
 ('Valentina','Rossi','vale.rossi@mail.com','+54 91123456789',1),
 ('Pedro','Ramirez Soto','pedro.ramirez@mail.com','+51 912345678',1),
 ('Camila','Fernandez','camila.f@mail.com','+56 987001122',0),
 ('Luis','Paredes Chavez','luis.paredes@mail.com','+51 955443322',1);

-- La reserva 1 (alojamiento 1, del 10/10 al 13/10) se usa en el caso de prueba CP09 de solapamiento
INSERT INTO reserva (entrada, salida, canal, precio, moneda, estado, idAlojamiento, idHuesped) VALUES
 ('2026-10-10 14:00:00','2026-10-13 11:00:00','AIRBNB', 540.0,'PEN','CONFIRMADA',1,1),
 ('2026-10-20 14:00:00','2026-10-23 11:00:00','BOOKING',540.0,'PEN','CONFIRMADA',1,2),
 ('2026-10-05 15:00:00','2026-10-08 11:00:00','DIRECTO',420.0,'PEN','CONFIRMADA',2,3),
 ('2026-10-15 14:00:00','2026-10-18 11:00:00','WEB',    660.0,'PEN','PENDIENTE', 3,4),
 ('2026-09-25 14:00:00','2026-09-30 11:00:00','AIRBNB',1300.0,'PEN','FINALIZADA',4,5),
 ('2026-10-25 14:00:00','2026-10-27 11:00:00','AIRBNB', 280.0,'PEN','CANCELADA', 2,6),
 ('2026-11-02 14:00:00','2026-11-06 11:00:00','DIRECTO',880.0,'PEN','CONFIRMADA',3,1);

INSERT INTO pago (tipo, importe, moneda, fecha, estado, idReserva) VALUES
 ('INGRESO', 540.0,'PEN','2026-10-01 10:15:00','PAGADO',   1),
 ('INGRESO', 270.0,'PEN','2026-10-02 18:40:00','PAGADO',   2),
 ('INGRESO', 420.0,'PEN','2026-10-05 15:30:00','PAGADO',   3),
 ('INGRESO', 330.0,'PEN','2026-10-06 09:00:00','PENDIENTE',4),
 ('INGRESO',1300.0,'PEN','2026-09-20 12:00:00','PAGADO',   5),
 ('GASTO',     60.0,'PEN','2026-09-30 16:00:00','PAGADO',   5),
 ('GASTO',     45.0,'PEN','2026-10-08 16:00:00','PAGADO',   3),
 ('INGRESO', 880.0,'PEN','2026-10-04 11:20:00','PAGADO',   7);

-- checklist: lista JSON de items { item, obligatorio, hecho } (US11)
INSERT INTO servicio (tipo, inicio, fin, estado, checklist, idAlojamiento, idReserva, idUsuario) VALUES
 ('LIMPIEZA','2026-09-30 12:00:00','2026-09-30 15:00:00','COMPLETADO',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":true},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":true},{"item":"Reponer amenities","obligatorio":false,"hecho":true}]',
  4,5,4),
 ('LIMPIEZA','2026-10-08 12:00:00','2026-10-08 15:00:00','ASIGNADO',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false},{"item":"Reponer amenities","obligatorio":false,"hecho":false}]',
  2,3,4),
 ('MANTENIMIENTO','2026-10-06 09:00:00','2026-10-06 11:00:00','EN_PROCESO',
  '[{"item":"Revisar fuga en el bano","obligatorio":true,"hecho":true},{"item":"Cambiar empaque de la llave","obligatorio":true,"hecho":false}]',
  1,NULL,5),
 ('LIMPIEZA','2026-10-13 12:00:00','2026-10-13 15:00:00','PENDIENTE',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false}]',
  1,1,4);

INSERT INTO evidencia (nombreArchivo, ruta, tipoArchivo, tamanio, fecha, idServicio, idUsuario) VALUES
 ('dormitorio-final.jpg','uploads/evidencias/demo-dormitorio-final.jpg','image/jpeg',245760,'2026-09-30 14:50:00',1,4),
 ('bano-final.jpg',      'uploads/evidencias/demo-bano-final.jpg',      'image/jpeg',198400,'2026-09-30 14:55:00',1,4);

INSERT INTO incidencia (categoria, prioridad, descripcion, estado, idAlojamiento, idReserva, idServicio) VALUES
 ('MANTENIMIENTO','ALTA',   'Fuga de agua en la llave del bano principal.',        'ABIERTA',    1,NULL,3),
 ('LIMPIEZA',     'MEDIA',  'El huesped reporta falta de toallas al ingresar.',    'EN_PROCESO', 2,3,   NULL),
 ('DANO',         'BAJA',   'Vaso roto en la cocina durante la estadia.',          'RESUELTA',   4,5,   1),
 ('SEGURIDAD',    'CRITICA','La cerradura de la puerta principal no cierra bien.', 'ABIERTA',    3,NULL,NULL);

INSERT INTO auditoria (usuario, fecha, entidad, operacion, idRegistro, detalle) VALUES
 ('admin','2026-10-01 09:00:00','usuarios','CREAR','6','Carga inicial de datos de prueba');
