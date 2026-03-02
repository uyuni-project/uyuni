DROP VIEW IF EXISTS rhnServerOutdatedPackages;

ALTER TABLE rhnErrata
    ALTER COLUMN advisory TYPE varchar(256),
    ALTER COLUMN advisory_name TYPE varchar(256);

CREATE OR REPLACE VIEW
rhnServerOutdatedPackages
(
    server_id,
    package_name_id,
    package_evr_id,
    package_arch_id,
    package_nvre,
    errata_id,
    errata_advisory
)
AS
SELECT DISTINCT SNC.server_id,
       P.name_id,
       P.evr_id,
       P.package_arch_id,
       PN.name || '-' || evr_t_as_vre_simple( PE.evr ),
       E.id,
       E.advisory
  FROM rhnPackageName PN,
       rhnPackageEVR PE,
       rhnPackage P,
       rhnServerNeededCache SNC
         left outer join
        rhnErrata E
          on SNC.errata_id = E.id
 WHERE SNC.package_id = P.id
   AND P.name_id = PN.id
   AND P.evr_id = PE.id;
