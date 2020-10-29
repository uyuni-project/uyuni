ALTER TABLE rhnActionDup DROP CONSTRAINT IF EXISTS rhn_actiondup_avc_ck;
ALTER TABLE rhnActionDup
  ADD COLUMN IF NOT EXISTS allow_vendor_change CHAR(1)
  DEFAULT ('N') NOT NULL
  CONSTRAINT rhn_actiondup_avc_ck
  CHECK (allow_vendor_change in ('Y','N'));
