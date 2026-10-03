# Epic 5 — Registro de lecturas de sensores IoT

API Spring Boot para recibir telemetría ambiental (temperatura y humedad), guardarla en PostgreSQL y consultar lecturas y estadísticas por sensor.

## Tecnologías

- Java 21
- Spring Boot 4
- Spring Data JPA
- Bean Validation
- PostgreSQL 16 (ejecución)
- H2 solo en los tests (`src/test/resources/application.properties`)

## Cómo levantar PostgreSQL

Con Docker, en la raíz del proyecto:

```powershell
docker compose up -d
```

Eso crea la base que ya usa `application.properties`:

| Dato | Valor |
|------|--------|
| Host | `localhost` |
| Puerto | `5432` |
| Base | `epic_telemetry` |
| Usuario | `postgres` |
| Contraseña | `postgres` |
| JDBC | `jdbc:postgresql://localhost:5432/epic_telemetry` |

Sin Docker, crea la misma base en el PostgreSQL que ya tengas (en esta máquina hay PostgreSQL 18 en el puerto 5432):

```sql
CREATE DATABASE epic_telemetry;
```

El usuario y la contraseña tienen que coincidir con `src/main/resources/application.properties`. El archivo trae `postgres` / `postgres`, que es lo que levanta Docker Compose. Si tu servidor local usa otra contraseña, cámbiala ahí (o con la variable `SPRING_DATASOURCE_PASSWORD`) antes de arrancar. Si el puerto 5432 ya está ocupado, Docker Compose no puede publicar el contenedor: para la base local o libera el puerto.

Hibernate crea o actualiza la tabla `sensor_readings` al arrancar (`ddl-auto=update`). La columna del valor se llama `measured_value` porque `value` es una palabra reservada en PostgreSQL; en el JSON el campo sigue siendo `value`.

## Cómo correr la API

Necesitas **JDK 21** en `JAVA_HOME`.

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`.

Los tests usan H2 en memoria y no necesitan PostgreSQL:

```powershell
.\mvnw.cmd test
```

## Estructura

```
src/main/java/com/epic/
├── EpicApplication.java
├── common/
│   └── ApiExceptionHandler.java
└── telemetry/
    ├── SensorReading.java
    ├── SensorReadingForm.java
    ├── SensorReadingStats.java
    ├── SensorReadingRepository.java
    ├── SensorReadingService.java
    └── SensorReadingController.java
```

```
Cliente
   ↓
Controller  → valida y responde HTTP
   ↓
Service     → reglas (timestamp de servidor, id de sensor)
   ↓
Repository  → PostgreSQL
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/readings` | Registrar una lectura. Responde `201` con el registro y su `id`. |
| `GET` | `/api/readings/sensor/{sensorId}` | Lecturas de ese sensor, de la más antigua a la más reciente. |
| `GET` | `/api/readings/sensor/{sensorId}/stats` | Cantidad, mínimo, máximo y promedio. |

`metric` solo acepta `TEMPERATURE` o `HUMIDITY`.

`sensorId` es el identificador del dispositivo: empieza con letra o número y luego puede llevar letras, números, `_` o `-` (máximo 64 caracteres). No hay catálogo aparte de sensores: un id con formato válido y sin lecturas devuelve lista vacía (o estadísticas en cero). Un id inválido responde `400`.

Si no envías `recordedAt`, el servidor usa su fecha y hora.

### POST — con marca de tiempo

```http
POST /api/readings
Content-Type: application/json
```

```json
{
  "sensorId": "sensor-01",
  "metric": "TEMPERATURE",
  "value": 23.5,
  "recordedAt": "2026-10-02T18:30:00"
}
```

Respuesta `201`:

```json
{
  "id": 1,
  "sensorId": "sensor-01",
  "metric": "TEMPERATURE",
  "value": 23.5000,
  "recordedAt": "2026-10-02T18:30:00"
}
```

### POST — sin marca de tiempo

```json
{
  "sensorId": "sensor-01",
  "metric": "HUMIDITY",
  "value": 61.25
}
```

### POST — error de validación (`400`)

```json
{
  "message": "Error de validación",
  "errors": {
    "sensorId": "El id del sensor es obligatorio",
    "value": "El valor medido es obligatorio"
  }
}
```

### GET — lecturas

```http
GET /api/readings/sensor/sensor-01
```

```json
[
  {
    "id": 1,
    "sensorId": "sensor-01",
    "metric": "TEMPERATURE",
    "value": 23.5000,
    "recordedAt": "2026-10-02T18:30:00"
  }
]
```

Si ese sensor no tiene lecturas: `200` y `[]`.

Si el id no es válido (`id con espacios`, `@@@`): `400`.

```json
{
  "message": "Error de validación",
  "errors": {
    "sensorId": "El id del sensor no es válido"
  }
}
```

### GET — estadísticas

```http
GET /api/readings/sensor/sensor-01/stats
```

```json
{
  "sensorId": "sensor-01",
  "count": 2,
  "min": 23.5000,
  "max": 27.0000,
  "average": 25.2500
}
```

Sin lecturas, `count` es `0` y `min`, `max` y `average` son `null`.

## Ejemplos en PowerShell

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/readings `
  -ContentType "application/json" `
  -Body '{"sensorId":"sensor-01","metric":"TEMPERATURE","value":23.5}'

Invoke-RestMethod -Uri http://localhost:8080/api/readings/sensor/sensor-01
Invoke-RestMethod -Uri http://localhost:8080/api/readings/sensor/sensor-01/stats
```
