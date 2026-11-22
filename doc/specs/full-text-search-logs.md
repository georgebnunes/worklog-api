# Spec: Full-text search on logs

**Story:** As a client of the work log API, I want to search my past work logs by words, so that I can quickly find what I did and report it to my manager.

**Scope:** Database layer only: a Flyway migration, a repository query and tests. No HTTP endpoint yet. It needs its own spec for path, response shape and paging.

## Divergences and known limits

- No search was described in the README or the earlier specs. This spec is new.
- English only: text is stemmed and filler words are ignored using the English rules. Logs in another language match poorly. Supporting one needs a new migration that rebuilds the searchable column.
- Whole words only. A part of a word (`depl`) does not find `deploy`. Typo tolerance is not covered either.
- No result cap, paging or limit for now. They are decided in the endpoint spec.

---

## Rule: Must keep a searchable form of every log's content in sync automatically, including logs that existed before.

- Example: The one where a log is registered. It can be found by search immediately, with no extra step from the application.
- Example: The one where logs already existed before the migration ran. They are searchable afterwards.
- Counter-example: The one where a log's content is changed. Search follows the new content and no longer finds the old words.

## Rule: Must match whole words in their root form (English), ignoring case and filler words.

| Stored content       | Search           | Found? |
|----------------------|------------------|--------|
| "deployed the fixes" | "deploy fix"     | yes (words reduced to their root) |
| "Deployed THE Fixes" | "deployed fixes" | yes (case ignored) |
| "deployed the fixes" | "depl"           | no (part of a word) |
| "deployed the fixes" | "the"            | no (filler word only) |

## Rule: Must require every word of a multi-word search to be present, unless the user writes `or`.

| Stored content              | Search             | Found? |
|-----------------------------|--------------------|--------|
| "deployed the fixes"        | "deploy fix"       | yes |
| "deployed the new release"  | "deploy fix"       | no (`fix` is missing) |
| "deployed the new release"  | "deploy or fix"    | yes |

## Rule: Must return only the logs of the account that searches.

- Example: The one where two accounts both logged "deployed the fixes". Each account finds only its own log.

## Rule: Must never return inactive logs.

- Example: The one where a log was deactivated. It is not returned, even for an exact match.

## Rule: Must rank results by relevance, and newest first when relevance is equal.

- Example: The one where one log mentions the searched words more prominently than another. It comes first.
- Counter-example: The one where two logs are equally relevant. The more recently created one comes first.

## Rule: Must treat the search text as typed by a person and never fail because of it.

| Search                 | Outcome |
|------------------------|---------|
| `"code review"`        | only logs containing that exact phrase |
| `deploy -rollback`     | logs with `deploy` and without `rollback` |
| `"unbalanced`          | empty list, no error (unbalanced quote) |
| blank or only spaces   | empty list |
| `the` (filler only)    | empty list |

## Rule: Must expose the searchable column in the `logs` table without changing the log entity.

- Example: The one where the Flyway migration `V3` runs. `logs` has a `contentSearch` column of type text-search vector, filled automatically from `content` using the English configuration, with an index for fast lookup. `V1` and `V2` are not edited.
- Counter-example: The one where the application saves a log. `contentSearch` is never written by the application, only by the database.
