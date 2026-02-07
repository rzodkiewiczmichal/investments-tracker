-- Add unique constraint on account name to prevent duplicates (FR-075)
ALTER TABLE accounts ADD CONSTRAINT accounts_name_unique UNIQUE (name);
