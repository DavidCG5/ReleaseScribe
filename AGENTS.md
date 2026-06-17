# AGENTS.md

ReleaseScribe: app web que genera release notes desde commits de git usando IA.

## Comandos
- Backend: `./mvnw spring-boot:run` (corre en :8080)
- Backend build/test: `./mvnw clean verify`
- Frontend: `cd frontend && npm install && npm run dev` (corre en :5173)
- DB local: `docker compose up -d` (Postgres en :5432)
- Migraciones: las maneja Flyway al arrancar el backend.

## Stack y versiones (no negociable)
- Java 21, Spring Boot 3.x, Maven.
- React 18 + Vite + TypeScript + TailwindCSS en /frontend.
- PostgreSQL 16. Acceso vía Spring Data JPA. Migraciones en /src/main/resources/db/migration.
- IA: API de OpenAI. La key SOLO desde la variable de entorno OPENAI_API_KEY.

## Convenciones
- Backend en capas: controller -> service -> repository. DTOs separados de las entidades.
- La llamada a la IA va aislada detrás de la interfaz AiClient. No llamar a la IA desde controllers.
- La IA debe devolver JSON estructurado; el Markdown final se arma en Java, no en el prompt.
- Frontend: componentes funcionales, hooks, Tailwind. Sin librerías de UI pesadas sin justificar.

## Límites (importante)
- NUNCA hardcodear API keys ni secretos. Usa .env (hay un .env.example).
- No añadir dependencias nuevas sin justificarlas.
- No borrar ni reescribir migraciones ya aplicadas; crea una nueva.

## Testing
- Tests del service de IA con el cliente mockeado (no llamar a la API real en tests).
- Correr `./mvnw verify` debe pasar antes de dar una tarea por terminada.