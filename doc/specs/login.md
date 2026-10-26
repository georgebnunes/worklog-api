# Spec: Login with JWT

**Story:** As a user of the work log API, I want to log in and get a stateless session with a JWT, so that I can register my work logs and manage my account.

**Endpoints:**

- `POST /worklog-api/v1/auth`: log in with email and password.
- `POST /worklog-api/v1/auth/refresh`: exchange a refresh token for a new JWT.

Login request:

```json
{"email":"bob@bob.com","password":"password123"}
```

Login response `200 OK`:

```json
{"token":"eyJhbGciOi...","expiresIn":1800,"refreshToken":"q3Vn2x..."}
```

Refresh request:

```json
{"refreshToken":"q3Vn2x..."}
```

The refresh response has the same shape as login.

## Divergences from the original story and README

- Protected endpoints take the JWT from a Bearer header only.
- The refresh exchange (`/auth/refresh`) is included, which the original story did not list.
- Refresh tokens are stored (hashed); the JWT is not stored.
- Brute-force protection (lockout or rate limiting) and logout are out of scope. Both are follow-up stories.

---

## Rule: Must authenticate an account from `email` and `password` and answer 200 with `token`, `expiresIn` and `refreshToken`.

- Example: The one where the email and password match an active account. The response is 200 with the JWT, `expiresIn` of 1800 seconds, and a refresh token.
- Counter-example: The one where the email is typed `Bob@Bob.com` and the account was registered as `bob@bob.com`. Login succeeds, because emails are compared ignoring case.

## Rule: Must reject wrong credentials with one identical answer (401), without revealing whether the email exists.

| email   | password | Outcome        |
|---------|----------|----------------|
| unknown | any      | 401, same body |
| known   | wrong    | 401, same body |
| known   | correct  | 200            |

## Rule: Must reject a missing or blank `email` or `password`, and a malformed body (400).

| email   | password                   | Outcome |
|---------|----------------------------|---------|
| missing | valid                      | 400     |
| valid   | blank                      | 400     |
| any     | body is not valid JSON     | 400     |
| known   | `pass123` (7 chars), wrong | 401     |

The last row shows that login does not check password length. A short wrong password is just a wrong password.

## Rule: Must refuse login for an inactive account, and allow an account whose email is unconfirmed.

| account state                 | Outcome |
|-------------------------------|---------|
| active, email unconfirmed     | 200     |
| inactive, correct credentials | 403     |

No token or refresh token is issued for a 403.

## Rule: Must issue a JWT signed with HS256 that identifies the account and expires 30 minutes after it is issued. (done)

- Example: The one where login succeeds at 10:00. The JWT carries the account id as its subject and expires at 10:30.
- Counter-example: The one where the signing secret is missing or shorter than 32 bytes. The application refuses to start.
- The secret comes from the `JWT_SECRET` environment variable, with a placeholder in `application.yaml` like the database settings.

## Rule: Must accept the JWT only from an `Authorization: Bearer` header on protected endpoints.

| Bearer header       | Outcome                  |
|---------------------|--------------------------|
| valid               | request is authenticated |
| none                | 401                      |
| expired or tampered | 401                      |

An account deactivated after login keeps a working JWT, but protected endpoints still answer 403 for it, as in `register-log.md`.

## Rule: Must issue an opaque refresh token that lives 7 days and is stored only as a hash.

- Example: The one where login succeeds. The response carries a random refresh token, and the database holds only its hash and its expiry (7 days after issue).
- Counter-example: The one where the database content is read. The refresh token cannot be recovered from it.

## Rule: Must exchange a valid refresh token for a new JWT and a new refresh token, and stop the old one from working (rotation).

| refresh token                          | Outcome                                           |
|----------------------------------------|---------------------------------------------------|
| valid, not used before                 | 200 with new JWT, `expiresIn`, new refresh token  |
| already used (rotated out)             | 401                                               |
| expired (older than 7 days)            | 401                                               |
| unknown or tampered                    | 401                                               |
| valid, but the account is now inactive | 403                                               |
| missing or blank                       | 400                                               |

## Rule: Must persist refresh tokens in a `refresh_tokens` table that follows the project conventions.

- Example: The one where the Flyway migration runs. The table has these columns:
  - `id`: integer primary key from a Postgres sequence.
  - `accountId`: integer, not null, references `accounts.id`.
  - `tokenHash`: not null, unique.
  - `expiresAt`: timestamp with timezone, not null.
  - `createdAt` and `updatedAt`: timestamps with timezone, defaulting to the current server time on insert.
  - `active`: boolean, defaults to `true`. A rotated-out token is set to `false`.
