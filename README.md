# ReleaseScribe

App web que genera **release notes** legibles a partir de commits de git, usando IA para clasificar y resumir.

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21 + Spring Boot 3.4 + Maven |
| Frontend | React 18 + TypeScript + Vite + TailwindCSS |
| Base de datos | PostgreSQL 16 |
| Migraciones | Flyway |
| IA | OpenAI API (GPT‑4o‑mini) |

## Requisitos

- Java 21 (JDK)
- Maven 3.9+ (o usar el Maven Wrapper incluido: `./mvnw`)
- Node.js 18+ y npm
- Docker Desktop (para PostgreSQL)
- Una API key de OpenAI

## Cómo arrancar

### 1. Base de datos

```bash
docker compose up -d
# PostgreSQL queda escuchando en localhost:5433
```

### 2. Backend

```bash
# Copiar y completar variables de entorno
cp .env.example .env
# Editar .env con tu OPENAI_API_KEY

# Compilar y arrancar
./mvnw spring-boot:run
# Backend disponible en http://localhost:8080
```

> **Nota**: si `JAVA_HOME` no apunta a JDK 21, el build puede fallar. Verificá con `java -version`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
# Frontend disponible en http://localhost:5173
```

El proxy de Vite redirige las llamadas a `/api` hacia el backend en `:8080`.

## Variables de entorno

| Variable | Default | Obligatoria | Descripción |
|----------|---------|-------------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/releasescribe` | No | URL de PostgreSQL |
| `DATABASE_USER` | `releasescribe` | No | Usuario de DB |
| `DATABASE_PASSWORD` | `releasescribe` | No | Contraseña de DB |
| `OPENAI_API_KEY` | — | **Sí** | API key de OpenAI |
| `OPENAI_MODEL` | `gpt-4o-mini` | No | Modelo de OpenAI |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/release-notes/generate` | Enviar commits, recibe Markdown clasificado |
| `GET` | `/api/release-notes` | Listar historial de notas |
| `GET` | `/api/release-notes/{id}` | Obtener detalle de una nota |

## Tests

```bash
./mvnw test
```

Los tests del servicio de IA usan un `AiClient` mockeado — nunca llaman a la API real.

## Estructura del proyecto

```
ReleaseScribe/
├── pom.xml
├── mvnw / mvnw.cmd
├── docker-compose.yml
├── src/main/java/com/releasescribe/
│   ├── config/          # Beans de configuración (CORS, RestTemplate)
│   ├── controller/      # Controladores REST
│   ├── dto/             # Objetos de transferencia
│   ├── model/           # Entidades JPA
│   ├── repository/      # Repositorios Spring Data
│   └── service/         # Lógica de negocio + IA
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/    # Migraciones Flyway
├── frontend/
│   └── src/
│       ├── api/         # Llamadas HTTP al backend
│       ├── components/  # Componentes React
│       └── types.ts     # Interfaces compartidas
└── README.md
```

## TODO (para futuro)

- [ ] Autenticación de usuarios
- [ ] Paginación en el historial
- [ ] Exportar como PDF
- [ ] Soporte para otros proveedores de IA
- [ ] Modo oscuro
