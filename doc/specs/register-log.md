# Spec: Register a work log

**Story:** As a client of the work log API, I want to register my work log, so that I can keep track and a history of my work.

**Endpoint:** `POST /worklog-api/v1/logs`

Request (with `Authorization: Bearer <JWT>`):

```json
{"content":"lorem ipsum dolor sit amet"}
```

Response `201 Created`:

```json
{"id":1,"content":"lorem ipsum dolor sit amet","userId":1,"active":true,"createdAt":"2026-10-21T21:44:44Z","updatedAt":"2026-10-21T21:44:44Z"}
```

## Divergences from the original story and README

- The maximum `content` length is 140 characters (the story said 150; the README says 140).
- The README curl sends `userId` in the body. The owner now comes only from the JWT, and a `userId` in the body is ignored.
- `createdAt` and `updatedAt` are UTC with a trailing `Z` (the sample had no zone).
- `id` is an INTEGER from a Postgres sequence, following the project convention, not a long integer.

---

## Rule: Must create a log from `content` for the account in the JWT, and answer 201 with `id`, `content`, `userId`, `active`, `createdAt` and `updatedAt`.

- Example: The one where an authenticated account sends valid content. The log is saved and the response is 201 with `userId` equal to the JWT subject and `active` true.
- Counter-example: The one where the body also sends `userId`, `active`, `createdAt` or `updatedAt`. They are ignored and the stored values come from the JWT and the server.

## Rule: Must reject a request without a valid Bearer JWT (401).

| Authorization header | Outcome |
|----------------------|---------|
| valid                | 201     |
| none                 | 401     |
| expired or tampered  | 401     |

## Rule: Must refuse an account that was deactivated after login (403) and save nothing.

- Example: The one where the account was deactivated after login. Its JWT still verifies, but the answer is 403 and no log is saved (as stated in `login.md`).

## Rule: Must trim `content`, then reject it when missing, empty, or longer than 140 characters (400).

| content                           | Outcome               |
|-----------------------------------|-----------------------|
| missing                           | 400                   |
| body is not valid JSON            | 400                   |
| `"   "` (spaces only)             | 400                   |
| 1 character                       | 201                   |
| 140 characters                    | 201                   |
| 141 characters                    | 400                   |
| `"  done  "` (surrounding spaces) | 201, stored as `done` |

The length limit applies after trimming and counts characters, not bytes.

## Rule: Must allow the same `content` to be registered more than once by the same account.

- Example: The one where an account registers "Daily standup" twice. Two separate logs exist with different `id`s.

## Rule: Must set `createdAt` and `updatedAt` from the server clock, equal at creation, and never accept them from the client.

- Example: The one where a log is created. `createdAt` and `updatedAt` are the same server time in UTC, ending in `Z`.
- Counter-example: The one where the client sends its own `createdAt` from last week. It is ignored, so a log cannot be backdated.

## Rule: Must persist logs in a `logs` table that follows the project conventions.

- Example: The one where the Flyway migration runs. The table has these columns:
  - `id`: integer primary key from a Postgres sequence.
  - `userId`: integer, not null, references `accounts.id`.
  - `content`: varchar(140), not null.
  - `createdAt` and `updatedAt`: timestamps with timezone, defaulting to the current server time on insert.
  - `active`: boolean, defaults to `true`.
