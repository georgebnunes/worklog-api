# Spec: List my work logs

**Story:** As a client of the work log API, I want to list my active work logs, newest first and page by page, so that I can review what I did and prepare my weekly report.

**Endpoint:** `GET /worklog-api/v1/logs`

Request (with `Authorization: Bearer <JWT>`), all query parameters optional: `page` (default 1), `size` (default 20, at most 100), `sort` (default `-createdAt`).

Response `200 OK`:

```json
{
  "data": {
    "items": [
      {"id":15,"content":"I'll work on the weeks agenda","active":true,"createdAt":"2026-11-22T20:04:56.323426Z","updatedAt":"2026-11-22T20:04:56.323426Z"}
    ],
    "page": 1,
    "size": 20,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

## Divergences and known limits

- The response is wrapped in `data`, while `POST /logs` answers a flat object. The register response is not changed here. Wrapping it is a separate spec.
- Only the filters in this spec exist. Searching by date or content comes later; the full-text search spec covers the database side of content search.

---

## Rule: Must list only the active logs of the account in the JWT, each with `id`, `content`, `active`, `createdAt` and `updatedAt`.

- Example: The one where an authenticated account with several logs asks for the list. The answer is 200 and `data.items` holds its active logs. Each item has exactly those five fields and never `userUUID`.
- Counter-example: The one where another account also has logs, or one of the account's own logs was deactivated. Neither appears in the list.

## Rule: Must answer 200 with an empty `items` list when the account has no active logs.

- Example: The one where an account has never registered a log, or all of its logs are inactive. The answer is 200 with `"items": []`, `page` 1, `size` 20, `totalItems` 0 and `totalPages` 0.

## Rule: Must refuse a request without a valid Bearer JWT with 401, reusing the existing security behaviour.

| Authorization header | Outcome |
|----------------------|---------|
| valid                | 200 |
| none                 | 401 |
| tampered             | 401 |
| expired              | 401, body `{"message":"session expired"}` |

## Rule: Must paginate with `page` (starting at 1, default 1) and `size` (default 20, at most 100), and report `page`, `size`, `totalItems` and `totalPages` inside `data` next to `items`.

- Example: The one where an account has 45 active logs and asks for `?page=2&size=20`. `items` holds the 21st to 40th logs, with `page` 2, `size` 20, `totalItems` 45 and `totalPages` 3.
- Counter-example: The one where the page asked for is past the last one (`?page=9` with 45 logs). The answer is 200 with empty `items` and the real `totalItems` 45 and `totalPages` 3, not an error.
- Counter-example: The one where the last page is not full (`?page=3&size=20` with 45 logs). `items` holds the remaining 5 logs.

## Rule: Must answer 400 with a message when `page`, `size` or `sort` has an invalid value, and return nothing.

| Request          | Outcome |
|------------------|---------|
| `?page=1`        | 200 |
| `?page=0`        | 400, `Page must be 1 or greater.` |
| `?page=abc`      | 400, `Page must be 1 or greater.` |
| `?size=100`      | 200 |
| `?size=101`      | 400, `Max page size allowed 100.` |
| `?size=0`        | 400, `Size must be 1 or greater.` |
| `?size=abc`      | 400, `Size must be 1 or greater.` |
| `?sort=content`  | 400, `Sort must be createdAt, +createdAt or -createdAt.` |

The 400 body has the same shape as the register validation error: `timestamp`, `status`, `error`, `message` and `path`.
The message texts above are proposals; they can change before implementation.

## Rule: Must sort by `createdAt`, newest first by default, and let the client choose the direction with `sort`.

| `sort`                                            | Outcome |
|---------------------------------------------------|---------|
| not sent                                          | newest first |
| `-createdAt`                                      | newest first |
| `+createdAt`, sent encoded (`%2BcreatedAt`)       | oldest first |
| `+createdAt`, sent bare (arrives as ` createdAt`) | oldest first |
| `createdAt` (no sign)                             | oldest first |

- Counter-example: The one where two logs have exactly the same `createdAt`. They keep a stable order by `id` in the same direction, so a log never appears on two pages or on none while paging.

## Rule: Must ignore any other query parameter for now.

- Example: The one where a client sends `?content=agenda`. The answer is the normal, unfiltered list.
