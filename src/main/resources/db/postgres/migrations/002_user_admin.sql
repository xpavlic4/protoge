-- Migration 002: add users.admin (global administrator flag, false for everyone by default).
--
-- Run once against an existing database, e.g.:
--   psql "$POSTGRES_URL" -v ON_ERROR_STOP=1 -f src/main/resources/db/postgres/migrations/002_user_admin.sql
--
-- The script is idempotent (safe to run again).
-- Fresh databases created from schema.sql already have this column.

ALTER TABLE users ADD COLUMN IF NOT EXISTS admin BOOLEAN NOT NULL DEFAULT FALSE;
