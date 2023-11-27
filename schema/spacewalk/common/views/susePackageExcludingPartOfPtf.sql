--
-- Returns the packages excluding those that are part of a PTF
--

CREATE OR REPLACE VIEW susePackageExcludingPartOfPtf AS
  WITH ptfPackages AS (
      SELECT pp.package_id
        FROM rhnpackagecapability pc
                  INNER JOIN rhnpackageprovides pp ON pc.id = pp.capability_id
       WHERE pc.name = 'ptf-package()'
  )
  SELECT pkg.*
    FROM rhnpackage pkg
            LEFT JOIN ptfPackages ptf ON pkg.id = ptf.package_id
   WHERE ptf.package_id IS NULL
;
