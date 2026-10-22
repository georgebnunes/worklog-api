# Spec: Register a work log

**Story:** As a client of the work log API, I want to register my work log, so that I can track the history of my work.

**Endpoint:** `POST /worklog-api/v1/logs`

The user is identified by a JWT access token sent in a cookie.

Request:

```json
{"content":"lorem ipsum dolor sit amet"}
```

Response `201 Created`:

```json
{"id":1,"content":"lorem ipsum dolor sit amet","userId":1,"active":true,"createdAt":"2026-10-21T21:44:44Z","updatedAt":"2026-10-21T21:44:44Z"}
```

## Divergences from the original story and README

- The README sends `userId` in the body; here it comes from the cookie.
- The README says 140 characters max and the story's column says 150. The API accepts at most 140 and the column holds 150.
- `logs.id` is `BIGINT`, an exception to the project's "integer id" convention.
- The sample timestamps have no offset; the response uses UTC with `Z`.
- Authentication is a JWT in a cookie, not a bearer header.

---

## Rule: Must create a log from `content` and answer 201 with `id`, `content`, `userId`, `active`, `createdAt` and `updatedAt`.

- Example: The one where the user is identified and `content` is valid. The log is saved as active and the response is 201 with all six fields.
- Counter-example: The one where the body also sends `id`, `userId`, `active`, `createdAt` or `updatedAt`. They are ignored.

## Rule: Must take the author from the JWT in the cookie, never from the request body.

- Example: The one where the cookie identifies user 1. The log is stored with `userId` 1.
- Counter-example: The one where the body says `userId: 2` and the cookie says user 1. The log belongs to user 1.

## Rule: Must reject a request without valid identification.

| Cookie                      | Outcome |
|-----------------------------|---------|
| missing                     | 401     |
| expired or tampered JWT     | 401     |
| valid JWT, account inactive | 403     |
| valid JWT, active account   | 201     |

Nothing is saved for a 401 or 403.

## Rule: Must reject a missing, empty or whitespace-only `content`, and a body that is not valid JSON (400).

| content / body    | Outcome |
|-------------------|---------|
| `content` missing | 400     |
| `""`              | 400     |
| `"   "`           | 400     |
| malformed JSON    | 400     |

## Rule: Must reject a `content` longer than 140 characters (400), after trimming surrounding whitespace.

| content                                  | Outcome             |
|------------------------------------------|---------------------|
| 1 character                              | 201                 |
| 140 characters                           | 201                 |
| 141 characters                           | 400                 |
| 140 characters plus surrounding spaces   | 201, stored trimmed |
| 140 characters including emoji (1 each) | 201                 |

Length counts user-visible characters, so an emoji counts as 1.

## Rule: Must allow a user to register many logs, including identical content.

- Example: The one where a user posts the same content twice. Two logs exist with different ids.
- Counter-example: The one where two users post the same content. Each log keeps its own `userId`.

## Rule: Must set `createdAt` and `updatedAt` to the same server time at creation.

- Example: The one where a log is created. `createdAt` equals `updatedAt` and both are the server's current time, returned in UTC with offset.

## Rule: Must persist logs in a `logs` table that follows the project conventions.

- Example: The one where the Flyway migration runs. The table has these columns:
  - `id`: `BIGINT` primary key from a Postgres sequence.
  - `content`: `VARCHAR(150)`, not null.
  - `userId`: `INTEGER`, not null, references `accounts.id`.
  - `createdAt` and `updatedAt`: timestamps with timezone, defaulting to the current server time on insert.
  - `active`: boolean, defaults to `true`.
