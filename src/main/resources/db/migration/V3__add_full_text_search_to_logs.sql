-- Searchable form of `content`, kept in sync by the database on every INSERT/UPDATE (no trigger, no application code).
-- Existing rows are back-filled when the column is added. Generated columns need the two-argument, immutable
-- to_tsvector, and queries must use the same 'english' configuration or they silently match nothing.
ALTER TABLE logs
    ADD COLUMN "contentSearch" TSVECTOR
        GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;
