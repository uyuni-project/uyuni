DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'suseproductsccrepository') THEN

      ALTER TABLE suseProductSCCRepository ADD COLUMN IF NOT EXISTS gpg_key_url  VARCHAR(256);
      ALTER TABLE suseProductSCCRepository ADD COLUMN IF NOT EXISTS gpg_key_id   VARCHAR(14);
      ALTER TABLE suseProductSCCRepository ADD COLUMN IF NOT EXISTS gpg_key_fp   VARCHAR(50);

    ELSE
      RAISE NOTICE 'suseProductSCCRepository does not exists';
    END IF;
  END;
$$;
