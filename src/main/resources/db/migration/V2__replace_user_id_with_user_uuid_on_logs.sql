-- The owner of a log is now the `sub` claim of the JWT issued by bn-auth-service, which is a UUID string, so the
-- integer "userId" can no longer hold it. NOT NULL fails if logs already has rows: delete or backfill them first.
ALTER TABLE logs DROP COLUMN "userId";
ALTER TABLE logs ADD COLUMN "userUUID" VARCHAR(180) NOT NULL;
