-- New view with all the packages excluding those that are part of a PTF
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

-- Update the view for the newest package to exclude packages part of a PTF
DROP VIEW IF EXISTS rhnChannelNewestPackageView;
CREATE VIEW rhnChannelNewestPackageView AS
SELECT channel_id,
       name_id,
       evr_id,
       package_arch_id,
       package_id
FROM (
      SELECT channel_id,
             name_id,
             evr_id,
             package_arch_id,
             build_time,
             max(package_id) as package_id,
             ROW_NUMBER() OVER(PARTITION BY name_id, channel_id, package_arch_id ORDER BY build_time DESC) rn
      FROM (
            SELECT m.channel_id          as channel_id,
                   p.name_id             as name_id,
                   p.evr_id              as evr_id,
                   m.package_arch_id     as package_arch_id,
                   p.id                  as package_id,
                   p.build_time          as build_time
            FROM (select max(pe.evr) AS max_evr,
                         cp.channel_id,
                         p.name_id,
                         p.package_arch_id
                    from rhnPackageEVR                           pe,
                         susePackageExcludingPartOfPtf            p,
                         suseChannelPackageRetractedStatusView   cp
                   where p.evr_id = pe.id
                     and cp.package_id = p.id
                     and NOT cp.is_retracted
                   group by cp.channel_id, p.name_id, p.package_arch_id) m,
                 rhnPackageEVR       pe,
                 rhnPackage          p,
                 rhnChannelPackage   chp
            WHERE m.max_evr = pe.evr
                AND m.name_id = p.name_id
                AND m.package_arch_id = p.package_arch_id
                AND p.evr_id = pe.id
                AND chp.package_id = p.id
                AND chp.channel_id = m.channel_id
      ) latest_packages
      group by channel_id, name_id, evr_id, package_arch_id, build_time
) n
WHERE rn = 1;

-- Update the function to refresh the updatable packages to exclude those that are part of a PTF from the results
UPDATE pg_settings
   SET setting = 'rhn_server,' || setting
 WHERE name = 'search_path';

    create or replace function update_needed_cache(
        server_id_in in numeric
  ) returns void as $$
    begin
      delete from rhnServerNeededCache
        where server_id = server_id_in;
      insert into rhnServerNeededCache
             (server_id, errata_id, package_id, channel_id)
        (select distinct sp.server_id, x.errata_id, p.id, x.channel_id
           FROM (SELECT sp_sp.server_id, sp_sp.name_id,
            sp_sp.package_arch_id, max(sp_pe.evr) AS max_evr
                   FROM rhnServerPackage sp_sp
                   join rhnPackageEvr sp_pe ON sp_pe.id = sp_sp.evr_id
                  GROUP BY sp_sp.server_id, sp_sp.name_id, sp_sp.package_arch_id) sp
           join susePackageExcludingPartOfPtf p ON p.name_id = sp.name_id
           join rhnPackageEvr pe ON pe.id = p.evr_id AND (sp.max_evr).type = (pe.evr).type AND sp.max_evr < pe.evr
           join rhnPackageUpgradeArchCompat puac
              ON puac.package_arch_id = sp.package_arch_id
        AND puac.package_upgrade_arch_id = p.package_arch_id
           join rhnServerChannel sc ON sc.server_id = sp.server_id
           join rhnChannelPackage cp ON cp.package_id = p.id
              AND cp.channel_id = sc.channel_id
           left join (SELECT ep.errata_id, ce.channel_id, ep.package_id
                        FROM rhnChannelErrata ce
                        join rhnErrataPackage ep
               ON ep.errata_id = ce.errata_id
      join rhnServerChannel sc_sc
               ON sc_sc.channel_id = ce.channel_id
           WHERE sc_sc.server_id = server_id_in) x
             ON x.channel_id = sc.channel_id AND x.package_id = cp.package_id
     left join rhnErrata e on x.errata_id = e.id
          where sp.server_id = server_id_in
            and (x.errata_id IS NULL or e.advisory_status != 'retracted')); -- packages which are part of a retracted errata should not be installed
  end$$ language plpgsql;

-- restore the original setting
UPDATE pg_settings
   SET setting = overlay( setting placing '' FROM 1 FOR (LENGTH('rhn_server') + 1) )
 WHERE name = 'search_path';
