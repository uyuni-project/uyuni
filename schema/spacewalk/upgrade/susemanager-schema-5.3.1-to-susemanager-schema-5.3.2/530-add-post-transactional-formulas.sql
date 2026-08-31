ALTER TABLE suseTransactionalActionHistory
    ADD COLUMN IF NOT EXISTS post_transactional_formulas JSONB;
