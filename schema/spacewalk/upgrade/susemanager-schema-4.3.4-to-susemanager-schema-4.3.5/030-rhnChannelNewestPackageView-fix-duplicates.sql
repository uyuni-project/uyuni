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
                         rhnPackage                              p,
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
