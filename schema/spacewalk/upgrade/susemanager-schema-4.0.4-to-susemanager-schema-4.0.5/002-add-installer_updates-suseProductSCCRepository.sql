ALTER TABLE suseProductSCCRepository DROP CONSTRAINT IF EXISTS suse_prdrepo_instup_ck;
ALTER TABLE suseProductSCCRepository ADD COLUMN IF NOT EXISTS
  installer_updates CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT suse_prdrepo_instup_ck CHECK (installer_updates in ('Y', 'N'));
