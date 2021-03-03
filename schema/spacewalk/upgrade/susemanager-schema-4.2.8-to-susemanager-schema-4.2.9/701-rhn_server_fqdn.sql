ALTER TABLE rhnServerFQDN ADD COLUMN IF NOT EXISTS is_primary CHAR(1) DEFAULT ('N') NOT NULL;

UPDATE rhnServerFQDN f1 SET is_primary = 'Y'
WHERE f1.name = (SELECT MIN(f2.name) FROM rhnServerFQDN f2 WHERE f2.server_id = f1.server_id)
AND NOT EXISTS (SELECT 1 FROM rhnServerFQDN f3 WHERE f3.server_id = f1.server_id AND is_primary='Y');

CREATE UNIQUE INDEX IF NOT EXISTS rhn_srv_fqdn_prim_fqdn
  ON rhnServerFQDN
  (server_id) where is_primary = 'Y';
