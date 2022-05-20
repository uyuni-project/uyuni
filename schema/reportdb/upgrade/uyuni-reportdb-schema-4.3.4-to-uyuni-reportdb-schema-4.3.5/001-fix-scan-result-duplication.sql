DO $$
  BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema."columns" WHERE table_name = 'xccdscanresult' AND column_name = 'ident_id') THEN

      -- Temporary sequence to fill the missing ident_id and allow it to be not null
      CREATE SEQUENCE IF NOT EXISTS ident_id_seq;

      ALTER TABLE XccdScanResult ADD COLUMN ident_id NUMERIC NOT NULL DEFAULT nextval('ident_id_seq');

      -- Drop the default value and the sequence
      ALTER TABLE XccdScanResult ALTER COLUMN ident_id DROP DEFAULT;

      DROP SEQUENCE IF EXISTS ident_id_seq;

    ELSE
      RAISE NOTICE 'xccdscanresult already contains the column ident_id';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.key_column_usage WHERE constraint_name = 'xccdscanresult_pk' AND column_name = 'ident_id') THEN

      ALTER TABLE XccdScanResult DROP CONSTRAINT IF EXISTS XccdScanResult_pk;

      ALTER TABLE XccdScanResult ADD CONSTRAINT XccdScanResult_pk PRIMARY KEY (mgm_id, scan_id, rule_id, ident_id);

    ELSE
      RAISE NOTICE 'xccdscanresult primary key already contains the column ident_id';
    END IF;
  END;
$$;
