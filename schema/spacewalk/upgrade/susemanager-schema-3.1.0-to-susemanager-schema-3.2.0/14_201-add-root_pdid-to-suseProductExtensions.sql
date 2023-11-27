
DO $$
  BEGIN
    alter table suseProductExtension add root_pdid numeric CONSTRAINT suse_prdext_rootid_fk REFERENCES suseProducts (id) ON DELETE CASCADE;
    update suseProductExtension set root_pdid = base_pdid;
    alter table suseProductExtension ALTER COLUMN root_pdid SET NOT NULL;

    CREATE UNIQUE INDEX prdext_ber_id_uq
    ON suseProductExtension (base_pdid, ext_pdid, root_pdid);

    alter table suseProductExtension add recommended CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT suse_prdext_rec_ck CHECK (recommended in ('Y', 'N'));
  EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column root_pdid already exists in suseProductExtension';
  END;
$$;
