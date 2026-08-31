ALTER TABLE suseTransactionalActionHistory
    ADD COLUMN IF NOT EXISTS post_transactional_formulas VARCHAR(1024);
