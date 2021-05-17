ALTER TABLE rhnOrgConfiguration DROP CONSTRAINT IF EXISTS rhn_org_conf_clm_sync_patches;

ALTER TABLE rhnOrgConfiguration ADD COLUMN IF NOT EXISTS clm_sync_patches CHAR(1)
    DEFAULT('Y') NOT NULL;

ALTER TABLE rhnOrgConfiguration ADD CONSTRAINT rhn_org_conf_clm_sync_patches
    CHECK (clm_sync_patches in ('Y', 'N'));
