# Euchre Backend

Spring Boot REST API for a single-player Euchre platform.

## Requirements

- Java 21
- Maven
- Docker / Docker Compose

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d
```

Start the API:

```bash
mvn spring-boot:run
```

The API runs on `http://localhost:8080`.

## Session Auth

Protected game endpoints require:

```http
X-Session-Token: <token from /api/player/login>
```

## Main Endpoints

- `POST /api/player/login`
- `POST /api/game/new`
- `GET /api/game/latest`
- `GET /api/game/{id}`
- `POST /api/game/{id}/play`
- `POST /api/game/{id}/trump`
- `POST /api/game/{id}/pass`
- `DELETE /api/game/{id}`
