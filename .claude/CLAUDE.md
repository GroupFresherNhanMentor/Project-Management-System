# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

A Jira-like project management system. The repo has two independent sub-projects:

- `BE/` — Spring Boot 4.1 / Java 25 REST API
- `FE/` — Angular 22 SSR single-page application

---

## Backend (BE/)

### Commands

```bash
# Run the app (Flyway migrations run automatically on startup)
./mvnw spring-boot:run

# Compile only
./mvnw clean compile

# Run tests (requires Docker for Testcontainers)
./mvnw test

# Run a single test class
./mvnw test -pl BE -Dtest=UserRepositoryTest

# Migrate DB then regenerate jOOQ classes (run after any schema change)
./mvnw flyway:migrate generate-sources

# Override DB connection (default: localhost:5432/mini-project-management-system, postgres/postgres)
./mvnw flyway:migrate generate-sources -Ddb.url=... -Ddb.username=... -Ddb.password=...
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Architecture

**Package-by-feature** — each domain (`user`, `project`, `sprint`, `task`, `comment`, `activity`, `worklog`, `projectmember`, `auth`, `dashboard`) is fully self-contained with its own `controller/`, `service/`, `repository/`, `dto/`, `mapper/` sub-packages.

**Strict call direction:** `Controller → Service → Repository`. Never call a repository directly from a controller, or cross-call between feature services.

**Data access layer (jOOQ):**
- `BaseRepository<R extends UpdatableRecord<R>>` in `common/repository/` provides `findById`, `findAll`, `create`, `update`, `deleteById`, `existsById`.
- Each feature's `*RepositoryImpl` extends `BaseRepository` and adds domain-specific queries using the DSL directly.
- `update(record)` calls `record.store()` — jOOQ uses dirty-tracking and appends `RETURNING *` (configured via `JooqConfig`) so DB-generated fields like `updated_at` are refreshed.
- The standard update pattern: `findById` → mutate fields via MapStruct `updateRecord()` → `repository.update(record)`.
- jOOQ classes are generated into `target/generated-sources/jooq/` — never edit them manually. Regenerate with `mvn flyway:migrate generate-sources` after any schema change.

**API response shape** — all endpoints return `ApiResponse<T>` (`{ data, message, isSuccess }`). Use `ApiResponse.success(data, message)` or `ApiResponse.error(message)`.

**Exception handling:**
- `AppException` (extends `RuntimeException`) → HTTP 409 Conflict
- `IllegalArgumentException` / `IllegalStateException` → HTTP 400
- `MethodArgumentNotValidException` (bean validation) → HTTP 400 with first field error message

**Security:** Stateless JWT via Spring OAuth2 Resource Server. Role-based authorization uses `@PreAuthorize("hasAuthority('ADMIN')")` at the method level. Public endpoints: `/api/auth/**`, `/actuator/**`, `/v3/api-docs/**`, `/swagger-ui/**`.

**`updated_at` on all tables** is set by a PostgreSQL `BEFORE UPDATE` trigger (`set_updated_at()`), not by application code. The `JooqConfig` bean enables `returnAllOnUpdatableRecord=true` so `store()` retrieves the trigger-updated value via `RETURNING *`.

**Tests** use `@SpringBootTest` + Testcontainers (`PostgreSQLContainer`) with `@ServiceConnection` — no mocking of the database. Each test class annotated `@Transactional` for automatic rollback.

**Lombok conventions:** all service/repository classes use `@RequiredArgsConstructor` + `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` instead of explicit constructors and field modifiers.

---

## Frontend (FE/)

### Commands

```bash
cd FE

# Dev server (SSR enabled, http://localhost:4200)
ng serve

# Type-check without building
npx tsc --noEmit

# Build
ng build

# Run tests
ng test
```

### Architecture

**Angular 22 standalone components** — no NgModules. Every component declares its own `imports: []` array.

**SSR (Server-Side Rendering) is active.** `ng serve` fires component lifecycle hooks in Node.js before the browser boots. Key rules:
- Never access `localStorage` outside `isPlatformBrowser(PLATFORM_ID)` guards.
- Both `authGuard` and `adminGuard` check `isPlatformBrowser` first and return `true` on the server to avoid SSR redirect loops.
- Parameterized routes (`/:id`) must be set to `RenderMode.Server` in `app.routes.server.ts` to avoid prerender errors.

**Current state: all API calls removed.** Components use hardcoded `MOCK_*` constants. The login page bypasses HTTP and writes a mock token/user directly to `localStorage`. This is intentional for UI development without a running backend.

**Signal-based state:**
- `ProjectContextService` — signal holding the selected project ID and user's project role (`PM` | `DEV` | `TESTER`). Set in `AppShell` constructor based on the logged-in username.
- `ToastService` — signal array of toasts, auto-dismissed after 2.6 s.
- `LoadingService` — signal-based global loading bar driven by `loadingInterceptor`.

**Routing structure:**
```
/login                  → Login (no shell)
/ (AppShell + authGuard)
  /dashboard            → role-aware dashboard
  /board                → kanban board
  /backlog              → backlog + inline task filter
  /sprints, /sprints/new, /sprints/:id
  /members
  /worklog, /worklog/:userId
  /projects, /projects/new
  /tasks/:id            → task detail (4 tabs: details / comments / worklog / activity)
  /users, /users/new    → admin only (adminGuard)
```

**Styling:** Tailwind CSS v4 with `@theme` tokens defined in `styles.css`. Use inline `style=` attributes for one-off colors from the design token palette (`#3B4FD9` primary, `#14161C` heading, `#F5F6F8` app-bg, etc.). CSS utility classes `.badge`, `.badge-{status}`, `.priority-{level}`, `.dot-{status}` are defined globally in `styles.css`.

**Shared utilities:**
- `InitialsPipe` — derives 2-letter initials from a full name for avatar circles.
- `app-toast` component — renders `ToastService.toasts()` in a fixed bottom-right stack.

**HTTP layer** (for when API integration resumes): `authInterceptor` attaches `Bearer` token and redirects to `/login` on 401. All API base paths are centralised in `FE/src/app/configs/api-endpoints.ts`.

**Design tokens (Manrope font, color palette)** come from the design handoff at `../Project management website design/`. The theme is applied globally via `@theme` in `styles.css` and Material density `-1` in `material-theme.scss`.
