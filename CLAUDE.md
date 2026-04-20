# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Step 1189는 성경 전체 1,189장의 읽기 진행 상황을 GitHub 잔디처럼 시각화하는 Spring Boot 웹 서비스입니다.

## Commands

```bash
# Build
mvn clean package

# Run (local profile - H2 in-memory DB)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=UserControllerTest

# Coverage report (generated after package)
mvn package  # report at target/site/jacoco/index.html

# Docker deploy
docker-compose up --build
```

## Profiles

| Profile | DB | data.sql |
|---|---|---|
| `local` | H2 in-memory (MySQL mode) | `always` (loaded on startup) |
| `dev` | MySQL via env vars | `never` |
| default | MySQL via env vars | `never` |

Local profile env vars not needed. Dev/prod requires `DB_URL`, `DB_USERNANE` (typo in yml), `DB_PASSWORD`, `OAUTH_CLIENT_ID`, `OAUTH_CLIENT_SECRET`.

## Architecture

### Layer Structure
```
controller/api/   — REST controllers + OAuth2SuccessHandler
service/          — Business logic (ProgressService interface + JpaProgressServiceImpl)
repository/       — Spring Data JPA repositories
entity/           — JPA entities: User, Bible, Progress, DailyProgress
dto/              — Request/response records (user/, progress/)
config/           — SecurityConfig, WebConfig
exception/        — Custom exceptions + WebControllerAdvice
model/            — Enums: Role, Testament
validator/        — UserEmailDuplicateValidator (Spring Validator)
```

### Authentication Flow
- **Local login**: Form login via Spring Security (`/login` → POST → redirect `/main`). Username field is `email`.
- **OAuth2 (Google)**: `OAuth2UserService` handles user creation/lookup on login. `OAuth2SuccessHandler` checks `Role`: if `PRE_USER` → redirect to `/signup/set-username` to collect username; otherwise → `/main`.
- **Roles**: `PRE_USER` = OAuth user who hasn't set a username yet; `USER` = fully registered; `ADMIN` = admin.
- `CustomUserDetails` wraps local users; OAuth endpoints use `@AuthenticationPrincipal OAuth2User`.

### Progress Storage
`Progress.progressData` stores chapter read counts as a comma-separated string (bitmap), e.g. `"2,1,0,0"` for a 4-chapter book. `JpaProgressServiceImpl` converts between this bitmap and `Map<Integer, Integer>` (chapter → count) on every read/write. No caching yet (Redis planned).

`DailyProgress` tracks per-day chapter count for the GitHub-style grass heatmap, returned via `GET /api/v1/main`.

### View Routing
`WebConfig` maps URL paths to Thymeleaf templates (no controller needed for static pages). API endpoints live under `/api/v1/`. The `BibleController` serves bible metadata.

### Validation
`UserEmailDuplicateValidator` is registered via `@InitBinder` in `UserController` and fires on `POST /api/v1/users`. Errors cause `EmailDuplicateException` to be thrown, caught by `WebControllerAdvice`.
