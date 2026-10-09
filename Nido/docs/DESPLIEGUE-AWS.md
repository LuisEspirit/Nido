# Despliegue de Nido en AWS

Arquitectura sencilla y apta para la capa gratuita:

```
Postman / Frontend ──HTTP :8080──▶ EC2 (Amazon Linux 2023, Java 25, Nido.jar)
                                         │ JDBC :3306 (solo desde la EC2)
                                         ▼
                                   RDS MySQL 8 (bd_nido)
```

> Alternativa más barata para la demo: instalar MySQL en la misma EC2 y omitir RDS (paso 1).

## 1. Base de datos en Amazon RDS

1. Consola de AWS → **RDS → Create database**.
   - Motor: **MySQL 8.0** (o 8.4). Plantilla: **Free tier**.
   - Identificador: `nido-db`. Usuario maestro: `admin`. Guarde la contraseña.
   - Clase: `db.t3.micro` / `db.t4g.micro`, 20 GB.
   - **Public access: No**.
   - Security group nuevo: `nido-db-sg`.
2. Cuando el estado sea *Available*, copie el **Endpoint** (por ejemplo
   `nido-db.xxxxxxxx.us-east-1.rds.amazonaws.com`).

## 2. Servidor EC2

1. **EC2 → Launch instance**.
   - AMI: **Amazon Linux 2023**. Tipo: `t3.micro` (o `t2.micro`).
   - Key pair: cree `nido-key` y descargue el `.pem`.
   - Security group `nido-app-sg` con reglas de entrada:
     - SSH (22) desde *My IP*.
     - TCP **8080** desde `0.0.0.0/0` (o solo desde las IP del equipo/profesor).
2. Vuelva al security group **`nido-db-sg`** y agregue la regla de entrada
   *MySQL/Aurora (3306)* con origen **`nido-app-sg`**. Así solo la EC2 puede llegar a la base.

## 3. Instalar Java y el cliente MySQL en la EC2

```bash
ssh -i nido-key.pem ec2-user@IP_PUBLICA_EC2
sudo dnf install -y java-25-amazon-corretto-headless mariadb105
java -version   # debe mostrar 25
```

## 4. Cargar la base de datos

Desde su PC copie el script y el jar (paso 5) a la EC2:

```bash
scp -i nido-key.pem database/bd_nido.sql ec2-user@IP_PUBLICA_EC2:~
```

En la EC2:

```bash
mysql -h ENDPOINT_RDS -u admin -p < bd_nido.sql
mysql -h ENDPOINT_RDS -u admin -p -e "select count(*) from bd_nido.usuario"   # 6
```

Recomendado: crear un usuario solo para la aplicación (no usar `admin`):

```sql
CREATE USER 'nido_app'@'%' IDENTIFIED BY 'UNA_CLAVE_SEGURA';
GRANT SELECT, INSERT, UPDATE, DELETE ON bd_nido.* TO 'nido_app'@'%';
```

## 5. Generar y subir el jar

En su PC, dentro de la carpeta `Nido`:

```bash
./mvnw clean package -DskipTests          # en Windows: mvnw.cmd clean package -DskipTests
scp -i nido-key.pem target/Nido-0.0.1-SNAPSHOT.jar ec2-user@IP_PUBLICA_EC2:~/nido.jar
```

## 6. Ejecutar Nido como servicio

En la EC2 cree el archivo de variables (con sus valores reales):

```bash
sudo mkdir -p /opt/nido/evidencias && sudo mv ~/nido.jar /opt/nido/
sudo tee /opt/nido/nido.env > /dev/null <<'EOF'
SPRING_DATASOURCE_URL=jdbc:mysql://ENDPOINT_RDS:3306/bd_nido?useSSL=true&serverTimezone=America/Lima&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=nido_app
SPRING_DATASOURCE_PASSWORD=UNA_CLAVE_SEGURA
NIDO_JWT_SECRET=CAMBIE_ESTO_POR_UNA_FRASE_LARGA_DE_AL_MENOS_32_CARACTERES
NIDO_EVIDENCIAS_DIR=/opt/nido/evidencias
EOF
sudo chmod 600 /opt/nido/nido.env
```

Servicio de systemd para que arranque solo y se reinicie si falla:

```bash
sudo tee /etc/systemd/system/nido.service > /dev/null <<'EOF'
[Unit]
Description=Nido API
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/nido
EnvironmentFile=/opt/nido/nido.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/nido/nido.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
sudo chown -R ec2-user /opt/nido
sudo systemctl daemon-reload
sudo systemctl enable --now nido
sudo journalctl -u nido -f      # esperar "Started NidoApplication"
```

## 7. Verificar

- Aplicación web: `http://IP_PUBLICA_EC2:8080/` (pantalla de login; usuarios de prueba con contraseña `Nido2026!`)
- Swagger: `http://IP_PUBLICA_EC2:8080/swagger-ui.html`
- Login:
  ```bash
  curl -X POST http://IP_PUBLICA_EC2:8080/api/v1/auth/login \
       -H "Content-Type: application/json" -d '{"login":"admin","password":"Nido2026!"}'
  ```
- Postman: en la colección cambie la variable `baseUrl` a `http://IP_PUBLICA_EC2:8080/api/v1`
  y ejecute el Runner (con la base recién cargada).

## 8. Después de desplegar (para la rúbrica)

1. **Landing:** en el repositorio `nido-landing`, edita `js/config.js` y cambia `NIDO_APP_URL` por
   `http://IP_PUBLICA_EC2:8080/login.html`. Así el botón "Iniciar sesión" de la landing abre la app en AWS.
2. **Evidencias para el documento (sección 4.2.2.5):**
   - Consola de AWS con la instancia EC2 *Running* y la base RDS *Available*.
   - `sudo systemctl status nido` mostrando *active (running)*.
   - Postman o el navegador llamando a `http://IP_PUBLICA_EC2:8080/api/v1/auth/login`.
   - Las variables de entorno **sin mostrar contraseñas**.
3. **Notion:** pasa la tarea WI-21 "Despliegue en AWS" a *Hecho*.

## Actualizar a una nueva versión

```bash
scp -i nido-key.pem target/Nido-0.0.1-SNAPSHOT.jar ec2-user@IP_PUBLICA_EC2:/opt/nido/nido.jar
ssh -i nido-key.pem ec2-user@IP_PUBLICA_EC2 "sudo systemctl restart nido"
```

## Notas

- **Zona horaria**: la aplicación trabaja en hora de Perú (`America/Lima`) aunque la EC2 esté en UTC.
  Si se necesitara otra zona, defina `NIDO_ZONA_HORARIA` en `nido.env`.
- **Fotos de evidencia**: se guardan en `NIDO_EVIDENCIAS_DIR` dentro de la EC2. Si la instancia se
  elimina se pierden; para producción conviene moverlas a S3.
- **Costos**: detenga la EC2 y la RDS cuando no las use (la RDS detenida se reinicia sola a los 7 días).
- **Seguridad**: nunca suba `nido.env`, el `.pem` ni contraseñas a GitHub.
