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
--      aramos     PROPIETARIO   ACTIVO
--      pvargas    PROPIETARIO   ACTIVO
--      jsalazar   PERSONAL      ACTIVO   (limpieza)
--      ecastillo  PERSONAL      ACTIVO   (mantenimiento)
--      kpalomino  PERSONAL      ACTIVO   (inspeccion)
--      snunez     ADMIN         ACTIVO
-- =====================================================================

SET NAMES utf8mb4;
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
  idUsuario     INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idAlojamiento),
  KEY fk_alojamiento_registro (idUsuario),
  KEY fk_alojamiento_usuario (idPropietario),
  KEY fk_alojamiento_ubigeo (idubigeo),
  CONSTRAINT fk_alojamiento_ubigeo  FOREIGN KEY (idubigeo)      REFERENCES ubigeo (idubigeo),
  CONSTRAINT fk_alojamiento_usuario FOREIGN KEY (idPropietario) REFERENCES usuario (idusuario),
  CONSTRAINT fk_alojamiento_registro FOREIGN KEY (idUsuario) REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE huesped (
  idHuesped      INT NOT NULL AUTO_INCREMENT,
  nombres        VARCHAR(100) DEFAULT NULL,
  apellidos      VARCHAR(100) DEFAULT NULL,
  correo         VARCHAR(100) DEFAULT NULL,
  telefono       VARCHAR(45)  DEFAULT NULL,
  consentimiento TINYINT(1)   DEFAULT NULL,
  idUsuario      INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idHuesped),
  KEY fk_huesped_registro (idUsuario),
  CONSTRAINT fk_huesped_registro FOREIGN KEY (idUsuario) REFERENCES usuario (idusuario)
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
  idUsuario     INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idReserva),
  KEY fk_reserva_registro (idUsuario),
  KEY fk_reserva_alojamiento (idAlojamiento),
  KEY fk_reserva_huesped (idHuesped),
  CONSTRAINT fk_reserva_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_reserva_huesped     FOREIGN KEY (idHuesped)     REFERENCES huesped (idHuesped),
  CONSTRAINT fk_reserva_registro    FOREIGN KEY (idUsuario)     REFERENCES usuario (idusuario)
) ENGINE=InnoDB;

CREATE TABLE pago (
  idPago    INT NOT NULL AUTO_INCREMENT,
  tipo      VARCHAR(45) DEFAULT NULL,
  importe   DOUBLE      DEFAULT NULL,
  moneda    VARCHAR(10) DEFAULT NULL,
  fecha     DATETIME    DEFAULT NULL,
  estado    VARCHAR(45) DEFAULT NULL,
  idReserva INT NOT NULL,
  idUsuario INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idPago),
  KEY fk_pago_reserva (idReserva),
  KEY fk_pago_registro (idUsuario),
  CONSTRAINT fk_pago_reserva  FOREIGN KEY (idReserva) REFERENCES reserva (idReserva),
  CONSTRAINT fk_pago_registro FOREIGN KEY (idUsuario) REFERENCES usuario (idusuario)
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
  idPersonal    INT NOT NULL COMMENT 'Personal operativo asignado',
  idUsuario     INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idServicio),
  KEY fk_servicio_alojamiento (idAlojamiento),
  KEY fk_servicio_reserva (idReserva),
  KEY fk_servicio_personal (idPersonal),
  KEY fk_servicio_registro (idUsuario),
  CONSTRAINT fk_servicio_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_servicio_reserva     FOREIGN KEY (idReserva)     REFERENCES reserva (idReserva),
  CONSTRAINT fk_servicio_personal    FOREIGN KEY (idPersonal)    REFERENCES usuario (idusuario),
  CONSTRAINT fk_servicio_registro    FOREIGN KEY (idUsuario)     REFERENCES usuario (idusuario)
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
  idUsuario     INT NOT NULL COMMENT 'Usuario que registra',
  PRIMARY KEY (idIncidencia),
  KEY fk_incidencia_registro (idUsuario),
  KEY fk_incidencia_alojamiento (idAlojamiento),
  KEY fk_incidencia_reserva (idReserva),
  KEY fk_incidencia_servicio (idServicio),
  CONSTRAINT fk_incidencia_alojamiento FOREIGN KEY (idAlojamiento) REFERENCES alojamiento (idAlojamiento),
  CONSTRAINT fk_incidencia_reserva     FOREIGN KEY (idReserva)     REFERENCES reserva (idReserva),
  CONSTRAINT fk_incidencia_servicio    FOREIGN KEY (idServicio)    REFERENCES servicio (idServicio),
  CONSTRAINT fk_incidencia_registro    FOREIGN KEY (idUsuario)     REFERENCES usuario (idusuario)
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
--  DATOS DE PRUEBA (al menos 10 registros por tabla)
--  La columna idUsuario indica el usuario que registro cada dato.
-- =====================================================================

INSERT INTO pais (iso, nombre) VALUES
 ('PE','Peru'), ('AR','Argentina'), ('BR','Brasil'), ('CL','Chile'),
 ('CO','Colombia'), ('MX','Mexico'), ('ES','Espana'), ('US','Estados Unidos'),
 ('EC','Ecuador'), ('BO','Bolivia'), ('UY','Uruguay'), ('FR','Francia');

INSERT INTO ubigeo (departamento, provincia, distrito) VALUES
 ('Lima','Lima','Miraflores'),
 ('Lima','Lima','Barranco'),
 ('Lima','Lima','San Isidro'),
 ('Cusco','Cusco','Cusco'),
 ('Arequipa','Arequipa','Yanahuara'),
 ('Lima','Lima','Santiago de Surco'),
 ('Lima','Lima','San Borja'),
 ('Lima','Lima','La Molina'),
 ('Cusco','Urubamba','Urubamba'),
 ('Ica','Pisco','Paracas'),
 ('Piura','Talara','Mancora'),
 ('Arequipa','Arequipa','Cayma');

INSERT INTO catalogo (descripcion, estado) VALUES
 ('Tipo de servicio','ACTIVO'),
 ('Categoria de incidencia','ACTIVO'),
 ('Canal de reserva','ACTIVO'),
 ('Moneda','ACTIVO'),
 ('Estado de reserva','ACTIVO'),
 ('Estado de servicio','ACTIVO'),
 ('Prioridad de incidencia','ACTIVO'),
 ('Tipo de pago','ACTIVO'),
 ('Estado de pago','ACTIVO'),
 ('Especialidad del personal','ACTIVO');

INSERT INTO datacatalogo (descripcion, estado, idCatalogo) VALUES
 ('LIMPIEZA','ACTIVO',1), ('MANTENIMIENTO','ACTIVO',1), ('INSPECCION','ACTIVO',1),
 ('LIMPIEZA','ACTIVO',2), ('MANTENIMIENTO','ACTIVO',2), ('DANO','ACTIVO',2), ('SEGURIDAD','ACTIVO',2), ('OTRO','ACTIVO',2),
 ('AIRBNB','ACTIVO',3), ('BOOKING','ACTIVO',3), ('DIRECTO','ACTIVO',3), ('WEB','ACTIVO',3),
 ('PEN','ACTIVO',4), ('USD','ACTIVO',4),
 ('PENDIENTE','ACTIVO',5), ('CONFIRMADA','ACTIVO',5), ('EN_CURSO','ACTIVO',5), ('FINALIZADA','ACTIVO',5), ('CANCELADA','ACTIVO',5),
 ('PENDIENTE','ACTIVO',6), ('ASIGNADO','ACTIVO',6), ('ACEPTADO','ACTIVO',6), ('RECHAZADO','ACTIVO',6), ('EN_PROCESO','ACTIVO',6), ('COMPLETADO','ACTIVO',6), ('CANCELADO','ACTIVO',6),
 ('BAJA','ACTIVO',7), ('MEDIA','ACTIVO',7), ('ALTA','ACTIVO',7), ('CRITICA','ACTIVO',7),
 ('INGRESO','ACTIVO',8), ('GASTO','ACTIVO',8),
 ('PAGADO','ACTIVO',9), ('PENDIENTE','ACTIVO',9), ('ANULADO','ACTIVO',9),
 ('LIMPIEZA','ACTIVO',10), ('MANTENIMIENTO','ACTIVO',10), ('INSPECCION','ACTIVO',10);

-- Los roles 4 a 10 quedan INACTIVOS: estan previstos para los siguientes sprints
INSERT INTO rol (nombre, estado) VALUES
 ('ADMIN','ACTIVO'),
 ('PROPIETARIO','ACTIVO'),
 ('PERSONAL','ACTIVO'),
 ('RECEPCIONISTA','INACTIVO'),
 ('CONTADOR','INACTIVO'),
 ('SUPERVISOR','INACTIVO'),
 ('ANFITRION','INACTIVO'),
 ('SOPORTE','INACTIVO'),
 ('AUDITOR','INACTIVO'),
 ('INVITADO','INACTIVO');

INSERT INTO opcion (nombre, estado, ruta, tipo) VALUES
 ('Panel','ACTIVO','/panel',1),
 ('Alojamientos','ACTIVO','/alojamientos',1),
 ('Reservas','ACTIVO','/reservas',1),
 ('Huéspedes','ACTIVO','/huespedes',1),
 ('Servicios','ACTIVO','/servicios',1),
 ('Incidencias','ACTIVO','/incidencias',1),
 ('Pagos e ingresos','ACTIVO','/pagos',1),
 ('Reportes','ACTIVO','/reportes',1),
 ('Usuarios y roles','ACTIVO','/usuarios',2),
 ('Auditoría','ACTIVO','/auditoria',2),
 ('Mis tareas','ACTIVO','/mis-tareas',3),
 ('Personal','ACTIVO','/personal',1);

INSERT INTO rol_has_opcion (idrol, idopcion) VALUES
 (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,12),
 (2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(2,12),
 (3,6),(3,11);

-- Contrasena de todos los usuarios de prueba: Nido2026!  (hash BCrypt)
INSERT INTO usuario (nombres, apellidos, dni, login, password, correo, fechaRegistro, fechaNacimiento, direccion, especialidad, estado, idubigeo) VALUES
 ('Administrador','Nido','70000001','admin',    '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','admin@nido.pe',    '2026-08-01 09:00:00','1990-01-15','Av. Larco 100',            NULL,           'ACTIVO',  1),
 ('Lucia','Mendoza Rojas','70000002','lmendoza','$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','lucia@nido.pe',    '2026-08-02 10:00:00','1987-04-22','Calle Schell 250',         NULL,           'ACTIVO',  1),
 ('Jorge','Quispe Huaman','70000003','jquispe', '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','jorge@nido.pe',    '2026-08-03 11:00:00','1982-11-03','Av. El Sol 320',           NULL,           'ACTIVO',  4),
 ('Rosa','Huaman Castro','70000004','rhuaman',  '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','rosa@nido.pe',     '2026-08-05 08:30:00','1994-06-10','Jr. Union 45',             'LIMPIEZA',     'ACTIVO',  2),
 ('Carlos','Flores Diaz','70000005','cflores',  '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','carlos@nido.pe',   '2026-08-06 08:30:00','1989-02-28','Av. Grau 780',             'MANTENIMIENTO','ACTIVO',  2),
 ('Maria','Torres Vega','70000006','mtorres',   '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','maria@nido.pe',    '2026-08-07 08:30:00','1996-09-14','Calle Lima 12',            'LIMPIEZA',     'INACTIVO',3),
 ('Andrea','Ramos Paredes','70000007','aramos', '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','andrea@nido.pe',   '2026-08-10 09:15:00','1991-03-08','Calle Jerusalen 220',      NULL,           'ACTIVO',  12),
 ('Pedro','Vargas Soto','70000008','pvargas',   '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','pedro@nido.pe',    '2026-08-12 16:40:00','1979-12-01','Av. Primavera 1450',       NULL,           'ACTIVO',  6),
 ('Julia','Salazar Rojas','70000009','jsalazar','$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','julia@nido.pe',    '2026-08-15 08:00:00','1998-07-19','Jr. Ayacucho 310',         'LIMPIEZA',     'ACTIVO',  6),
 ('Eduardo','Castillo Lima','70000010','ecastillo','$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','eduardo@nido.pe','2026-08-16 08:00:00','1985-05-25','Av. Aviacion 2100',        'MANTENIMIENTO','ACTIVO',  7),
 ('Karen','Palomino Cruz','70000011','kpalomino','$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','karen@nido.pe',   '2026-08-18 08:00:00','1993-10-30','Calle Mercaderes 118',     'INSPECCION',   'ACTIVO',  5),
 ('Sofia','Nunez Prado','70000012','snunez',    '$2a$10$wIA/3Y1vJpDcetS1JBSvwOeIrET0JBtZbu/aOVkwqP4iAs9F6ZZ9S','sofia@nido.pe',    '2026-08-20 09:00:00','1988-08-17','Av. Javier Prado 980',     NULL,           'ACTIVO',  3);

INSERT INTO usuario_has_rol (idusuario, idrol) VALUES
 (1,1), (2,2), (3,2), (4,3), (5,3), (6,3), (7,2), (8,2), (9,3), (10,3), (11,3), (12,1);

INSERT INTO alojamiento (nombre, direccion, latitud, longitud, capacidad, precioBase, estado, idPropietario, idubigeo, idUsuario) VALUES
 ('Departamento Miraflores Vista Mar','Malecon de la Reserva 610, Miraflores', -12.1318, -77.0300, 4, 180.0, 'DISPONIBLE', 2, 1, 2),
 ('Loft Barranco Bohemio',            'Jr. Batallon Ayacucho 271, Barranco',  -12.1490, -77.0215, 2, 140.0, 'DISPONIBLE', 2, 2, 2),
 ('Suite Ejecutiva San Isidro',       'Av. Camino Real 390, San Isidro',      -12.0975, -77.0365, 3, 220.0, 'DISPONIBLE', 3, 3, 3),
 ('Casa Andina Centro Cusco',         'Calle Saphi 840, Cusco',               -13.5150, -71.9820, 6, 260.0, 'DISPONIBLE', 3, 4, 3),
 ('Studio Yanahuara',                 'Calle Jerusalen 105, Yanahuara',       -16.3890, -71.5450, 2, 110.0, 'INACTIVO',   2, 5, 2),
 ('Penthouse Surco Golf',             'Av. El Golf 455, Santiago de Surco',   -12.1102, -76.9905, 5, 250.0, 'DISPONIBLE', 2, 6, 2),
 ('Cabana Valle Sagrado',             'Carretera Urubamba km 2, Urubamba',    -13.3047, -72.1160, 4, 210.0, 'DISPONIBLE', 3, 9, 3),
 ('Departamento Cayma Vista al Misti','Av. Bolognesi 512, Cayma',             -16.3830, -71.5480, 3, 130.0, 'DISPONIBLE', 7, 12, 7),
 ('Loft Yanahuara Colonial',          'Calle Lima 210, Yanahuara',            -16.3880, -71.5460, 2, 120.0, 'DISPONIBLE', 7, 5, 7),
 ('Casa de Playa Paracas',            'Lote 14, Bahia de Paracas',            -13.8320, -76.2510, 8, 380.0, 'DISPONIBLE', 8, 10, 8),
 ('Bungalow Mancora Sunset',          'Antigua Panamericana Norte km 1164',   -4.1040,  -81.0520, 4, 300.0, 'DISPONIBLE', 8, 11, 8),
 ('Mini Departamento San Borja',      'Av. San Luis 2240, San Borja',         -12.0920, -76.9990, 2, 115.0, 'DISPONIBLE', 8, 7, 1);

INSERT INTO huesped (nombres, apellidos, correo, telefono, consentimiento, idUsuario) VALUES
 ('Ana','Garcia Lopez','ana.garcia@mail.com','+51 987654321',1,2),
 ('John','Smith','john.smith@mail.com','+1 2025550143',1,2),
 ('Valentina','Rossi','vale.rossi@mail.com','+54 91123456789',1,2),
 ('Pedro','Ramirez Soto','pedro.ramirez@mail.com','+51 912345678',1,3),
 ('Camila','Fernandez','camila.f@mail.com','+56 987001122',0,3),
 ('Luis','Paredes Chavez','luis.paredes@mail.com','+51 955443322',1,3),
 ('Diego','Herrera Campos','diego.herrera@mail.com','+51 944112233',1,2),
 ('Emma','Wilson','emma.wilson@mail.co.uk','+44 7700900123',1,2),
 ('Lucas','Martin','lucas.martin@mail.fr','+33 612345678',1,3),
 ('Sofia','Delgado Ruiz','sofia.delgado@mail.com','+57 3001234567',1,7),
 ('Mateo','Silva Costa','mateo.silva@mail.com.br','+55 11987654321',1,7),
 ('Gabriela','Torres Medina','gabriela.torres@mail.com','+51 966778899',1,8);

-- La reserva 1 (alojamiento 1, del 10/10 al 13/10) se usa en el caso de prueba CP09 de solapamiento
INSERT INTO reserva (entrada, salida, canal, precio, moneda, estado, idAlojamiento, idHuesped, idUsuario) VALUES
 ('2026-10-10 14:00:00','2026-10-13 11:00:00','AIRBNB', 540.0,'PEN','CONFIRMADA',1,1,2),
 ('2026-10-20 14:00:00','2026-10-23 11:00:00','BOOKING',540.0,'PEN','CONFIRMADA',1,2,2),
 ('2026-10-05 15:00:00','2026-10-08 11:00:00','DIRECTO',420.0,'PEN','CONFIRMADA',2,3,2),
 ('2026-10-15 14:00:00','2026-10-18 11:00:00','WEB',    660.0,'PEN','PENDIENTE', 3,4,3),
 ('2026-09-25 14:00:00','2026-09-30 11:00:00','AIRBNB',1300.0,'PEN','FINALIZADA',4,5,3),
 ('2026-10-25 14:00:00','2026-10-27 11:00:00','AIRBNB', 280.0,'PEN','CANCELADA', 2,6,2),
 ('2026-11-02 14:00:00','2026-11-06 11:00:00','DIRECTO',880.0,'PEN','CONFIRMADA',3,1,3),
 ('2026-10-02 14:00:00','2026-10-05 11:00:00','AIRBNB', 750.0,'PEN','FINALIZADA',6,7,2),
 ('2026-10-16 14:00:00','2026-10-20 11:00:00','BOOKING',1000.0,'PEN','CONFIRMADA',6,8,2),
 ('2026-10-08 14:00:00','2026-10-11 11:00:00','DIRECTO',630.0,'PEN','EN_CURSO',  7,9,3),
 ('2026-10-01 14:00:00','2026-10-04 11:00:00','AIRBNB', 390.0,'PEN','FINALIZADA',8,10,7),
 ('2026-10-12 14:00:00','2026-10-14 11:00:00','WEB',    240.0,'PEN','CONFIRMADA',9,11,7),
 ('2026-10-09 14:00:00','2026-10-12 11:00:00','DIRECTO',1140.0,'PEN','CONFIRMADA',10,12,8),
 ('2026-10-22 14:00:00','2026-10-26 11:00:00','AIRBNB',1200.0,'PEN','PENDIENTE', 11,3,8),
 ('2026-10-03 14:00:00','2026-10-06 11:00:00','BOOKING',345.0,'PEN','FINALIZADA',12,6,1);

INSERT INTO pago (tipo, importe, moneda, fecha, estado, idReserva, idUsuario) VALUES
 ('INGRESO', 540.0,'PEN','2026-10-01 10:15:00','PAGADO',   1,2),
 ('INGRESO', 270.0,'PEN','2026-10-02 18:40:00','PAGADO',   2,2),
 ('INGRESO', 420.0,'PEN','2026-10-05 15:30:00','PAGADO',   3,2),
 ('INGRESO', 330.0,'PEN','2026-10-06 09:00:00','PENDIENTE',4,3),
 ('INGRESO',1300.0,'PEN','2026-09-20 12:00:00','PAGADO',   5,3),
 ('GASTO',    60.0,'PEN','2026-09-30 16:00:00','PAGADO',   5,3),
 ('GASTO',    45.0,'PEN','2026-10-08 16:00:00','PAGADO',   3,2),
 ('INGRESO', 880.0,'PEN','2026-10-04 11:20:00','PAGADO',   7,3),
 ('INGRESO', 750.0,'PEN','2026-09-28 19:05:00','PAGADO',   8,2),
 ('INGRESO', 500.0,'PEN','2026-10-06 13:45:00','PAGADO',   9,2),
 ('INGRESO', 630.0,'PEN','2026-10-07 10:30:00','PAGADO',  10,3),
 ('INGRESO', 390.0,'PEN','2026-09-29 08:50:00','PAGADO',  11,7),
 ('GASTO',    40.0,'PEN','2026-10-04 17:20:00','PAGADO',  11,7),
 ('INGRESO',1140.0,'PEN','2026-10-05 21:10:00','PAGADO',  13,8),
 ('INGRESO', 600.0,'PEN','2026-10-07 12:00:00','PENDIENTE',14,8),
 ('INGRESO', 345.0,'PEN','2026-10-01 09:30:00','PAGADO',  15,1);

-- checklist: lista JSON de items { item, obligatorio, hecho } (US11). idPersonal: personal asignado (US10)
INSERT INTO servicio (tipo, inicio, fin, estado, checklist, idAlojamiento, idReserva, idPersonal, idUsuario) VALUES
 ('LIMPIEZA','2026-09-30 12:00:00','2026-09-30 15:00:00','COMPLETADO',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":true},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":true},{"item":"Reponer amenities","obligatorio":false,"hecho":true}]',
  4,5,4,3),
 ('LIMPIEZA','2026-10-08 12:00:00','2026-10-08 15:00:00','ASIGNADO',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false},{"item":"Reponer amenities","obligatorio":false,"hecho":false}]',
  2,3,4,2),
 ('MANTENIMIENTO','2026-10-06 09:00:00','2026-10-06 11:00:00','EN_PROCESO',
  '[{"item":"Revisar fuga en el bano","obligatorio":true,"hecho":true},{"item":"Cambiar empaque de la llave","obligatorio":true,"hecho":false}]',
  1,NULL,5,2),
 ('LIMPIEZA','2026-10-13 12:00:00','2026-10-13 15:00:00','PENDIENTE',
  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false}]',
  1,1,4,2),
 ('LIMPIEZA',     '2026-10-05 12:00:00','2026-10-05 15:00:00','COMPLETADO','[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":true},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":true},{"item":"Barrer y trapear pisos","obligatorio":true,"hecho":true},{"item":"Reponer amenities","obligatorio":false,"hecho":true}]', 6,8,9,2),
 ('INSPECCION',   '2026-10-04 11:30:00','2026-10-04 12:30:00','COMPLETADO','[{"item":"Revisar inventario","obligatorio":true,"hecho":true},{"item":"Revisar danos visibles","obligatorio":true,"hecho":true},{"item":"Verificar llaves y cerraduras","obligatorio":true,"hecho":true}]', 8,11,11,7),
 ('LIMPIEZA',     '2026-10-06 12:00:00','2026-10-06 15:00:00','COMPLETADO','[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":true},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":true},{"item":"Barrer y trapear pisos","obligatorio":true,"hecho":true},{"item":"Reponer amenities","obligatorio":false,"hecho":true}]', 12,15,9,8),
 ('MANTENIMIENTO','2026-10-07 09:00:00','2026-10-07 12:00:00','EN_PROCESO','[{"item":"Diagnosticar el problema","obligatorio":true,"hecho":true},{"item":"Realizar la reparacion","obligatorio":true,"hecho":false},{"item":"Probar el funcionamiento","obligatorio":true,"hecho":false}]',  10,NULL,10,8),
 ('LIMPIEZA',     '2026-10-11 12:00:00','2026-10-11 15:00:00','ACEPTADO',  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false},{"item":"Barrer y trapear pisos","obligatorio":true,"hecho":false},{"item":"Reponer amenities","obligatorio":false,"hecho":false}]', 7,10,9,3),
 ('LIMPIEZA',     '2026-10-20 12:00:00','2026-10-20 15:00:00','ASIGNADO',  '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false},{"item":"Barrer y trapear pisos","obligatorio":true,"hecho":false},{"item":"Reponer amenities","obligatorio":false,"hecho":false}]', 6,9,4,2),
 ('INSPECCION',   '2026-10-14 12:00:00','2026-10-14 13:00:00','ASIGNADO',  '[{"item":"Revisar inventario","obligatorio":true,"hecho":false},{"item":"Revisar danos visibles","obligatorio":true,"hecho":false},{"item":"Verificar llaves y cerraduras","obligatorio":true,"hecho":false}]', 9,12,11,7),
 ('MANTENIMIENTO','2026-10-03 09:00:00','2026-10-03 11:00:00','RECHAZADO', '[{"item":"Diagnosticar el problema","obligatorio":true,"hecho":false},{"item":"Realizar la reparacion","obligatorio":true,"hecho":false},{"item":"Probar el funcionamiento","obligatorio":true,"hecho":false}]', 11,NULL,10,8),
 ('LIMPIEZA',     '2026-10-12 12:00:00','2026-10-12 15:00:00','PENDIENTE', '[{"item":"Cambiar sabanas y toallas","obligatorio":true,"hecho":false},{"item":"Limpiar bano y cocina","obligatorio":true,"hecho":false},{"item":"Barrer y trapear pisos","obligatorio":true,"hecho":false},{"item":"Reponer amenities","obligatorio":false,"hecho":false}]', 10,13,4,8);

-- Fotos de ejemplo incluidas en database/evidencias-demo (rutas relativas a la carpeta del proyecto)
INSERT INTO evidencia (nombreArchivo, ruta, tipoArchivo, tamanio, fecha, idServicio, idUsuario) VALUES
 ('dormitorio-final.jpg',   'database/evidencias-demo/dormitorio-final.jpg',   'image/jpeg',27302,'2026-09-30 14:50:00',1,4),
 ('bano-final.jpg',         'database/evidencias-demo/bano-final.jpg',         'image/jpeg',26157,'2026-09-30 14:55:00',1,4),
 ('fuga-bano-inicio.jpg',   'database/evidencias-demo/fuga-bano-inicio.jpg',   'image/jpeg',29652,'2026-10-06 09:20:00',3,5),
 ('sala-final.jpg',         'database/evidencias-demo/sala-final.jpg',         'image/jpeg',26926,'2026-10-05 14:30:00',5,9),
 ('cocina-final.jpg',       'database/evidencias-demo/cocina-final.jpg',       'image/jpeg',27904,'2026-10-05 14:40:00',5,9),
 ('terraza-final.jpg',      'database/evidencias-demo/terraza-final.jpg',      'image/jpeg',28227,'2026-10-05 14:45:00',5,9),
 ('inventario-cayma.jpg',   'database/evidencias-demo/inventario-cayma.jpg',   'image/jpeg',26128,'2026-10-04 12:10:00',6,11),
 ('cerradura-cayma.jpg',    'database/evidencias-demo/cerradura-cayma.jpg',    'image/jpeg',28238,'2026-10-04 12:20:00',6,11),
 ('dormitorio-sanborja.jpg','database/evidencias-demo/dormitorio-sanborja.jpg','image/jpeg',28802,'2026-10-06 14:35:00',7,9),
 ('bano-sanborja.jpg',      'database/evidencias-demo/bano-sanborja.jpg',      'image/jpeg',28639,'2026-10-06 14:40:00',7,9),
 ('aire-diagnostico.jpg',   'database/evidencias-demo/aire-diagnostico.jpg',   'image/jpeg',30972,'2026-10-07 09:40:00',8,10),
 ('aire-repuesto.jpg',      'database/evidencias-demo/aire-repuesto.jpg',      'image/jpeg',29165,'2026-10-07 10:30:00',8,10);

INSERT INTO incidencia (categoria, prioridad, descripcion, estado, idAlojamiento, idReserva, idServicio, idUsuario) VALUES
 ('MANTENIMIENTO','ALTA',   'Fuga de agua en la llave del bano principal.',         'ABIERTA',    1,NULL,3,   5),
 ('LIMPIEZA',     'MEDIA',  'El huesped reporta falta de toallas al ingresar.',     'EN_PROCESO', 2,3,   NULL,2),
 ('DANO',         'BAJA',   'Vaso roto en la cocina durante la estadia.',           'RESUELTA',   4,5,   1,   4),
 ('SEGURIDAD',    'CRITICA','La cerradura de la puerta principal no cierra bien.',  'ABIERTA',    3,NULL,NULL,3),
 ('LIMPIEZA',     'BAJA',   'Falta papel higienico en el bano de visitas.',         'RESUELTA',   6,8,   5,   9),
 ('MANTENIMIENTO','MEDIA',  'El aire acondicionado de la sala hace ruido.',         'EN_PROCESO', 10,NULL,8,  10),
 ('DANO',         'ALTA',   'Mancha de vino en el sofa de la sala.',                'ABIERTA',    8,11,  NULL,7),
 ('SEGURIDAD',    'ALTA',   'La ventana del dormitorio no cierra con seguro.',      'ABIERTA',    7,10,  NULL,3),
 ('OTRO',         'BAJA',   'El huesped solicita check-out tardio a las 2 p. m.',   'CERRADA',    12,15, NULL,8),
 ('MANTENIMIENTO','CRITICA','Olor a gas en la cocina; se cerro la llave general.',  'RESUELTA',   11,NULL,12, 8),
 ('LIMPIEZA',     'MEDIA',  'Olor a humedad en el bano principal.',                 'ABIERTA',    9,NULL,NULL,11),
 ('DANO',         'BAJA',   'El control remoto del televisor no funciona.',         'EN_PROCESO', 6,8,   5,   2);

INSERT INTO auditoria (usuario, fecha, entidad, operacion, idRegistro, detalle) VALUES
 ('admin',    '2026-10-01 09:00:00','usuarios',    'CREAR',     '12','POST /api/v1/usuarios'),
 ('lmendoza', '2026-10-01 10:15:00','pagos',       'CREAR',     '1', 'POST /api/v1/pagos'),
 ('lmendoza', '2026-10-01 18:30:00','alojamientos','ACTUALIZAR','6', 'PUT /api/v1/alojamientos/6'),
 ('jquispe',  '2026-10-02 08:45:00','reservas',    'CREAR',     '10','POST /api/v1/reservas'),
 ('aramos',   '2026-10-02 11:20:00','huespedes',   'CREAR',     '10','POST /api/v1/huespedes'),
 ('pvargas',  '2026-10-03 07:55:00','servicios',   'CREAR',     '12','POST /api/v1/servicios'),
 ('ecastillo','2026-10-03 08:30:00','servicios',   'ACTUALIZAR','12','PATCH /api/v1/servicios/12/estado'),
 ('jsalazar', '2026-10-05 14:30:00','servicios',   'CREAR',     NULL,'POST /api/v1/servicios/5/evidencias'),
 ('jsalazar', '2026-10-05 14:50:00','servicios',   'ACTUALIZAR','5', 'PATCH /api/v1/servicios/5/estado'),
 ('pvargas',  '2026-10-06 10:00:00','incidencias', 'ACTUALIZAR','10','PATCH /api/v1/incidencias/10/estado'),
 ('lmendoza', '2026-10-07 09:10:00','reservas',    'ACTUALIZAR','6', 'PATCH /api/v1/reservas/6/cancelar'),
 ('admin',    '2026-10-07 17:00:00','usuarios',    'ACTUALIZAR','6', 'PATCH /api/v1/usuarios/6/estado');
