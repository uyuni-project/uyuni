CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS

SELECT DISTINCT sasp.package_id AS pid, sc.server_id AS sid
FROM rhnserverchannel sc
    INNER JOIN suseappstream sas ON sas.channel_id = sc.channel_id
    INNER JOIN suseappstreampackage sasp ON sasp.module_id = sas.id
    LEFT JOIN suseserverappstream ssa ON ssa.name = sas.name
        AND ssa.stream = sas.stream
        AND ssa.arch = sas.arch
        AND ssa.server_id = sc.server_id
WHERE ssa.id IS NULL

UNION

SELECT DISTINCT p.id AS pid, server_stream.server_id AS sid
FROM suseServerAppstream server_stream
    INNER JOIN suseAppstream appstream ON appstream.name = server_stream.name
        AND appstream.stream = server_stream.stream
        AND appstream.arch = server_stream.arch
    INNER JOIN suseAppstreamApi api ON api.module_id = appstream.id
    inner join rhnPackageName pn ON pn.name = api.rpm
    inner join rhnPackage p ON p.name_id = pn.id
WHERE NOT EXISTS (
    SELECT package_id
    FROM suseServerAppStreamPackageView
    WHERE server_id = server_stream.server_id
        AND package_id = p.id
);

create or replace view
rhnChannelNewestPackageView
as
SELECT channel_id,
       name_id,
       evr_id,
       package_arch_id,
       package_id,
       appstream_id
FROM (
      SELECT channel_id,
             name_id,
             evr_id,
             package_arch_id,
             build_time,
             max(package_id) as package_id,
             appstream_id,
             ROW_NUMBER() OVER(PARTITION BY name_id, channel_id, package_arch_id, appstream_id ORDER BY build_time DESC) rn
      FROM (
            SELECT m.channel_id         AS channel_id,
                   p.name_id            AS name_id,
                   p.evr_id             AS evr_id,
                   m.package_arch_id    AS package_arch_id,
                   p.id                 AS package_id,
                   p.build_time         AS build_time,
                   m.appstream_id       AS appstream_id
            FROM (
                SELECT MAX(pe.evr) AS max_evr,
                       cp.channel_id,
                       p.name_id,
                       p.package_arch_id,
                       appstream.id AS appstream_id
                FROM rhnPackageEVR pe
                    INNER JOIN susePackageExcludingPartOfPtf p ON p.evr_id = pe.id
                    INNER JOIN suseChannelPackageRetractedStatusView cp ON cp.package_id = p.id
                    LEFT JOIN (
                        SELECT a.id AS id, a.channel_id AS channel_id, ap.package_id AS package_id
                        FROM suseAppStreamPackage ap
                        INNER JOIN suseAppStream a ON a.id = ap.module_id
                    ) appstream ON appstream.package_id = p.id AND appstream.channel_id = cp.channel_id
                    WHERE NOT cp.is_retracted
                    GROUP BY cp.channel_id, p.name_id, p.package_arch_id, appstream.id
                ) m,
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
      GROUP BY channel_id, name_id, evr_id, package_arch_id, build_time, appstream_id
) n
WHERE rn = 1;
