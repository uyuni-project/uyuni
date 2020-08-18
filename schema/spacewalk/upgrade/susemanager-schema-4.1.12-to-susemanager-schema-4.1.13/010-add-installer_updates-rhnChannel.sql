ALTER TABLE rhnChannel DROP CONSTRAINT IF EXISTS rhn_channel_instup_ck;
ALTER TABLE rhnChannel ADD COLUMN IF NOT EXISTS
  installer_updates CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_channel_instup_ck CHECK (installer_updates in ('Y', 'N'));
