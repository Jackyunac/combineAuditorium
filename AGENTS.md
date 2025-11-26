# Repository Guidelines

## Project Structure & Modules
- `backend/`: Spring Boot 3 Maven service; domain code lives in `src/main/java/com/combine/auditorium` with controllers/services/mappers, and config in `src/main/resources/application.yml` plus mapper XMLs.
- `frontend/`: Vue 3 + TypeScript + Vite; UI is under `src` (`components`, `views`, `router`, `stores`, `api`).
- `docker/`: MySQL, Redis, MinIO, and SRS config/volumes; `docker-compose.yml` starts local infra.
- Generated outputs (`backend/target`, `frontend/node_modules`) stay untracked.

## Environment & Configuration
- Tooling: Java 17 + Maven; Node 18+ with npm (use the provided `package-lock.json`).
- Override `backend/src/main/resources/application.yml` values via env vars when possible (e.g., `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATA_REDIS_HOST`, `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `GOOGLE_AI_API_KEY`, `FFMPEG_PATH`).
- Keep secrets out of commits; prefer `.env` or CI secrets. Local FFmpeg path must be set if not on PATH.

## Build, Test, and Run
- Backend: `cd backend && mvn clean package` builds the JAR; `mvn spring-boot:run` launches the API; `mvn test` executes JUnit-based unit/integration tests.
- Frontend: `cd frontend && npm install`; `npm run dev -- --host` for local dev; `npm run build` creates the production bundle; `npm run preview` serves the built assets for smoke checks.
- Infra: from repo root, `docker-compose up -d mysql redis minio srs` brings up dependencies expected by the app.

## Coding Style & Naming Conventions
- Java: 4-space indent; packages follow `com.combine.auditorium.*`; REST controllers end with `Controller`, services with `Service`/`ServiceImpl`, DTOs with `DTO`, and mappers mirror table names. Return the shared `Result` envelope from controllers and keep config in `config/`.
- Vue/TS: use `<script setup lang="ts">`, PascalCase component files, and kebab-case route paths. Place shared API clients in `src/api`, global styles in `style.css`, and keep state in Pinia stores under `src/stores`.

## Testing Guidelines
- Backend: prefer JUnit 5 + Spring Boot Test; name test classes `*Tests`; mock external services (Redis/MinIO/SRS/FFmpeg/Google AI) or use test containers; cover both success and failure paths for controllers/services.
- Frontend: no runner is configured; include manual verification notes (routes touched, browsers) in PRs, or add Vitest before landing significant UI logic.

## Commit & Pull Request Guidelines
- No git history is present; default to Conventional Commits (e.g., `feat: add live room playback`, `fix: handle expired JWT`).
- PRs should include a concise summary, linked issue/plan item, screenshots for UI changes, sample API requests/responses for backend changes, and the commands/tests you ran. Call out any config, schema, or Docker changes that affect local setup.
