# Finance Data Processing and Access Control Backend

Spring Boot 4 backend for a finance dashboard: JWT authentication, role-based access, financial record CRUD with filtering, and aggregated dashboard APIs. Data is persisted with **JPA/Hibernate**; local development uses **MySQL** as configured in `application.properties`.

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8 with a database named `financedata` (or override the JDBC URL)

## Configuration

| Property / env | Purpose |
|----------------|---------|
| `MYSQL_USER`, `MYSQL_PASSWORD` | Database credentials. Set `MYSQL_PASSWORD` in your environment before running (required if your MySQL user has a password). Default MySQL user is `root` if unset. |
| `JWT_SECRET` | Secret phrase for signing tokens (hashed with SHA-256 for HMAC key material). Optional prefix `base64:` for raw key bytes. |
| `JWT_EXPIRATION_MS` | Token lifetime (default 24 hours) |
| `APP_SEED_ENABLED` | If `true` (default), inserts demo users and sample rows when `app_users` is empty |

## Run

```bash
mvn spring-boot:run
```

Application context name: `FinanceDataProcessing` (default port **8080**).

## Demo users (when seeding runs)

Seeding is skipped if any user already exists. Default accounts:

| Email | Password | Role |
|-------|----------|------|
| `admin@demo.local` | `Admin12345!` | ADMIN |
| `analyst@demo.local` | `Analyst12345!` | ANALYST |
| `viewer@demo.local` | `Viewer12345!` | VIEWER |

## Authentication

1. Call `POST /api/auth/login` (see below for JSON body).
2. Copy the `token` from the JSON response.
3. For every other request, send header: `Authorization: Bearer <token>`  
   In Postman: **Authorization → Type: Bearer Token** and paste the token.

## Roles and access

| Capability | VIEWER | ANALYST | ADMIN |
|------------|--------|---------|-------|
| `GET /api/dashboard/summary` | Yes | Yes | Yes |
| `GET /api/financial-records` (list/filter) | No | Yes | Yes |
| `GET /api/financial-records/{id}` | No | Yes | Yes |
| `POST/PUT/DELETE` financial records | No | No | Yes |
| `GET/POST/PUT /api/users/**` | No | No | Yes |

Inactive users cannot authenticate (Spring Security `disabled` account).

**Enum values used in JSON**

- `role` (users): `VIEWER`, `ANALYST`, `ADMIN`
- `type` (financial records): `INCOME`, `EXPENSE`

---

## API reference — order to test (Postman)

**Base URL:** `http://localhost:8080` (change if you use another port.)

Use **Content-Type: `application/json`** for requests that have a body.

### Step 1 — Login (no Bearer token)

`POST /api/auth/login`

```json
{
  "email": "admin@demo.local",
  "password": "Admin12345!"
}
```

Response includes `token`, `tokenType`, `expiresAt`, and `user`. Use **`analyst@demo.local` / `Analyst12345!`** or **`viewer@demo.local` / `Viewer12345!`** to test other roles.

---

### Step 2 — Dashboard (VIEWER, ANALYST, or ADMIN)

`GET /api/dashboard/summary`

Optional query parameters (defaults shown):

- `trendMonths` — default `12` (how many months back for trend)
- `recentLimit` — default `10` (how many recent records in the response)

Example URL:

```http
GET http://localhost:8080/api/dashboard/summary?trendMonths=6&recentLimit=5
```

No request body.

---

### Step 3 — List financial records (ANALYST or ADMIN only)

`GET /api/financial-records`

Optional query parameters:

- `from`, `to` — ISO dates `YYYY-MM-DD`
- `category` — string (case-insensitive match)
- `type` — `INCOME` or `EXPENSE`
- Pagination: `page`, `size`, `sort` (Spring Data), e.g. `page=0&size=10`

Example:

```http
GET http://localhost:8080/api/financial-records?from=2025-01-01&to=2026-12-31&type=INCOME&page=0&size=20
```

No request body.

---

### Step 4 — Get one financial record (ANALYST or ADMIN)

`GET /api/financial-records/{id}`

Replace `{id}` with a real id from the list response. No request body.

Example:

```http
GET http://localhost:8080/api/financial-records/1
```

---

### Step 5 — Create financial record (ADMIN only)

`POST /api/financial-records`

```json
{
  "amount": 150.75,
  "type": "EXPENSE",
  "category": "Utilities",
  "recordDate": "2026-04-01",
  "notes": "Electric bill"
}
```

`amount` must be at least **0.01**. `notes` may be omitted or `null`.

Minimal example (no notes):

```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "recordDate": "2026-04-04"
}
```

---

### Step 6 — Update financial record (ADMIN only)

`PUT /api/financial-records/{id}`

```json
{
  "amount": 200.00,
  "type": "EXPENSE",
  "category": "Utilities",
  "recordDate": "2026-04-02",
  "notes": "Updated note"
}
```

---

### Step 7 — Delete financial record (ADMIN only, soft delete)

`DELETE /api/financial-records/{id}`

No request body. Returns **204 No Content** on success.

---

### Step 8 — List users (ADMIN only)

`GET /api/users`

No request body.

---

### Step 9 — Get one user (ADMIN only)

`GET /api/users/{id}`

No request body.

Example:

```http
GET http://localhost:8080/api/users/1
```

---

### Step 10 — Create user (ADMIN only)

`POST /api/users`

```json
{
  "email": "newuser@demo.local",
  "password": "SecurePass123",
  "fullName": "New User",
  "role": "ANALYST"
}
```

`password` must be at least **8** characters.

---

### Step 11 — Update user (ADMIN only)

`PUT /api/users/{id}`

All fields are optional; include only what you want to change:

```json
{
  "fullName": "Updated Name",
  "role": "VIEWER",
  "active": true,
  "password": "NewPassword123"
}
```

Example: deactivate a user without changing password:

```json
{
  "active": false
}
```

---

### Quick role check (optional)

| Login as | Expected |
|----------|----------|
| `viewer@demo.local` | Dashboard **GET** works; **GET** `/api/financial-records` → **403** |
| `analyst@demo.local` | Dashboard + financial **GET** work; **POST/PUT/DELETE** financial → **403** |
| `admin@demo.local` | All endpoints above |

## Validation and errors

Bean Validation on request DTOs; failures return **400** with `fieldErrors`. Duplicate user email → **409**. Missing entity → **404**. Invalid login → **401**. Role violations → **403** (JSON body from security handlers or controller advice).

## Assumptions and tradeoffs

- **Global records**: Summaries and lists include all non-deleted records in the database (not scoped per end user). Suitable for a single-organization dashboard.
- **Monthly trend**: Implemented by aggregating DB rows in memory over the selected range; fine for modest setups. For very large tables, SQL `GROUP BY` per month would be preferable.
- **Tests**: `src/test/resources/application-test.properties` uses **in-memory H2** (MySQL compatibility mode) so CI does not require MySQL. Production-style runs use MySQL.
- **Security**: JWT stateless API; HTTPS and token refresh are not in scope for this exercise.

## Project layout (high level)

- `domain` — JPA entities and enums
- `repository` — Spring Data JPA + specifications for filtering
- `service` — business logic and mapping
- `controller` — REST endpoints
- `config` — security, seed data
- `security` — JWT filter and helpers
- `exception` — centralized error responses
