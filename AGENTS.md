# AGENTS.md

ReleaseScribe: app web que genera release notes legibles a partir de commits de git, usando IA para clasificar y resumir.

## Comandos

- Backend (dev): `./mvnw spring-boot:run` → http://localhost:8080
- Backend (build + test): `./mvnw clean verify`
- Frontend (dev): `cd frontend && npm install && npm run dev` → http://localhost:5173
- Base de datos: `docker compose up -d` (Postgres en :5432)
- Migraciones: las aplica Flyway automáticamente al arrancar el backend.

## Stack y versiones (no negociable)

- Java 21 · Spring Boot 3.x · Maven
- React 18 · Vite · TypeScript · TailwindCSS (en /frontend)
- PostgreSQL 16 · Spring Data JPA · migraciones en src/main/resources/db/migration
- IA: API de OpenAI. La key se lee SOLO de la variable de entorno OPENAI_API_KEY.

## Convenciones

- Backend en capas: controller → service → repository. DTOs separados de las entidades JPA.
- La llamada a la IA va aislada detrás de la interfaz `AiClient`. No llamar a la IA desde controllers.
- La IA debe devolver JSON estructurado; el Markdown final se arma en código Java, no en el prompt.
- Frontend: componentes funcionales + hooks + Tailwind. Sin librerías de UI pesadas sin justificar.

## Límites

- Nunca hardcodear API keys ni secretos. Usar variables de entorno (ver .env.example).
- No agregar dependencias nuevas sin justificarlas.
- No editar ni borrar migraciones ya aplicadas; crear siempre una nueva.

## Testing

- Tests del servicio de IA con el cliente mockeado (nunca llamar a la API real en tests).
- `./mvnw verify` debe pasar antes de dar una tarea por terminada.
