ALTER TABLE rhnPackage
    ADD COLUMN IF NOT EXISTS is_ptf
        BOOLEAN DEFAULT (FALSE),
    ADD COLUMN IF NOT EXISTS is_part_of_ptf
        BOOLEAN DEFAULT (FALSE);

CREATE INDEX IF NOT EXISTS rhn_package_is_ptf_idx ON rhnPackage (is_ptf);
CREATE INDEX IF NOT EXISTS rhn_package_is_part_of_ptf_idx ON rhnPackage (is_part_of_ptf);

UPDATE rhnPackage SET is_ptf = TRUE WHERE id IN (
    SELECT DISTINCT pk.id
    FROM rhnpackage pk
        INNER JOIN rhnpackageprovides pp ON pk.id = pp.package_id
        INNER JOIN rhnpackagecapability pc ON pp.capability_id = pc.id
    WHERE pc.name = 'ptf()');

UPDATE rhnPackage SET is_part_of_ptf = TRUE WHERE id IN (
    SELECT DISTINCT pk.id
    FROM rhnpackage pk
        INNER JOIN rhnpackageprovides pp ON pk.id = pp.package_id
        INNER JOIN rhnpackagecapability pc ON pp.capability_id = pc.id
    WHERE pc.name = 'ptf-package()');

CREATE OR REPLACE VIEW susePackageExcludingPartOfPtf AS
  SELECT pkg.*
    FROM rhnpackage pkg
   WHERE pkg.is_part_of_ptf = FALSE
;
