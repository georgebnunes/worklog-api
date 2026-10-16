# Spec: Register an account

**Story:** As a client of the work log API, I want to register user accounts, so that users can have an account and manage their content.

**Endpoint:** `POST /worklog-api/v1/accounts`

Request:

```json
{"username":"user1","password":"password123","email":"bob@bob.com"}
```

Response `201 Created`:

```json
{"id":1,"username":"user1","email":"bob@bob.com"}
```

## Divergences from the original story

- The password is not returned in the response.
- The path uses the `/worklog-api` prefix.
- The sample password `pass123` is 7 characters, so it fails the 8-character minimum.

---

## Rule: Must create an account from `username`, `password` and `email`, and answer 201 with `id`, `username` and `email`.

- Example: The one where all three fields are valid and unused. The account is saved and the response is 201 without the password.
- Counter-example: The one where the request also sends `active` or `emailConfirmed`. They are ignored.

## Rule: Must start every new account with email unconfirmed, and not expose that flag.

- Example: The one where a registration succeeds. `emailConfirmed` is stored as `false` and the response does not show it.
- Counter-example: The one where the client sends `emailConfirmed: true`. It is ignored and the stored value stays `false`.

## Rule: Must reject a registration with a missing or blank field (400).

| username | password | email   | Outcome |
|----------|----------|---------|---------|
| missing  | valid    | valid   | 400     |
| valid    | blank    | valid   | 400     |
| valid    | valid    | missing | 400     |

## Rule: Must reject a username that does not follow the format (400).

| username                         | Outcome                |
|----------------------------------|------------------------|
| `ab` (2 characters)              | 400                    |
| `abc` (3 characters)             | 201                    |
| 30 characters                    | 201                    |
| 31 characters                    | 400                    |
| `user 1` or `user@1`             | 400                    |
| `john_doe.1`                     | 201                    |
| `" user1 "` (surrounding spaces) | 201, stored as `user1` |

## Rule: Must reject a password shorter than 8 or longer than 72 characters (400).

| password length        | Outcome |
|------------------------|---------|
| 7 (e.g. `pass123`)     | 400     |
| 8                      | 201     |
| 72                     | 201     |
| 73                     | 400     |

## Rule: Must reject an email that is not well formed (400).

- Example: The one where the email is `bob.com` (no `@`). The result is 400.
- Counter-example: The one where the email is `bob+work@bob.co.uk`. It is valid and accepted.

## Rule: Must reject a username or email that is already registered, ignoring case (409 Conflict).

- Example: The one where `User1` is registered and `user1` is submitted. The result is 409 and the original account is untouched.
- Example: The one where `Bob@Bob.com` is registered and `bob@bob.com` is submitted under another username. The result is 409.
- Counter-example: The one where a new username and a new email are submitted. Both accounts coexist.

## Rule: Must never store the password in plain text.

- Example: The one where a registration succeeds. The stored value is a salted hash that differs from the submitted password.

## Rule: Must persist accounts in an `accounts` table that follows the project conventions.

- Example: The one where the Flyway migration runs. The table has these columns:
  - `id`: integer primary key from a Postgres sequence.
  - `username`, `password` (hash) and `email`.
  - `emailConfirmed`: defaults to `false`.
  - `createdAt` and `updatedAt`: timestamps with timezone, defaulting to the current server time on insert.
  - `active`: defaults to `true`.
- Username and email are unique, compared case-insensitively.
