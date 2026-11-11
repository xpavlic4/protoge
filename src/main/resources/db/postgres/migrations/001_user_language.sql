-- Migration 001: add users.language (the user's preferred UI language, e.g. "en", "cs").
--
-- Run once against an existing database, e.g.:
--   psql "$POSTGRES_URL" -v ON_ERROR_STOP=1 -f src/main/resources/db/postgres/migrations/001_user_language.sql
--
-- The script is idempotent (safe to run again).
-- Fresh databases created from schema.sql already have this column.

ALTER TABLE users ADD COLUMN IF NOT EXISTS language VARCHAR(10);
