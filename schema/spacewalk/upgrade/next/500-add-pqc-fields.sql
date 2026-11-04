
-- add column to enable PQC signing
ALTER TABLE rhnContentSource ADD COLUMN IF NOT EXISTS
    pqc_metadata_signed CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_cs_pqcms_ck
    CHECK (pqc_metadata_signed in ( 'Y' , 'N' ));

-- add column to enable PQC signing
ALTER TABLE suseSCCRepository ADD COLUMN IF NOT EXISTS
    pqc_signed CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT suse_sccrepo_pqcsig_ck
    CHECK (pqc_signed in ('Y', 'N'));

-- add column to enable PQC signing
ALTER TABLE rhnChannel ADD COLUMN IF NOT EXISTS
    pqc_check CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_channel_pqc_ck
    CHECK (pqc_check in ('Y', 'N'));

-- add column to enable PQC signing
ALTER TABLE suseISSHub ADD COLUMN IF NOT EXISTS
    pqc_cert TEXT;