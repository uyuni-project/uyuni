CREATE OR REPLACE VIEW suseServerAppStreamPackageView AS
SELECT
    p.package_id,
    c.id AS channel_id,
    c.label AS channel_label,
    s.server_id
FROM suseAppstream m
    JOIN suseAppstreamPackage p ON p.module_id = m.id
    JOIN rhnchannel c ON m.channel_id = c.id
    LEFT JOIN suseServerAppstream s
        ON  m.name = s.name
        AND m.stream = s.stream
        AND m.arch = s.arch;

CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS
SELECT DISTINCT sasp.package_id AS pid, sc.server_id AS sid
FROM rhnserverchannel sc
    INNER JOIN suseappstream sas ON sas.channel_id = sc.channel_id
    INNER JOIN suseappstreampackage sasp ON sasp.module_id = sas.id
    LEFT JOIN suseserverappstream ssa ON ssa.name = sas.name
        AND ssa.stream = sas.stream
        AND sas.arch = ssa.arch
WHERE ssa.id IS NULL

UNION

SELECT DISTINCT p.id AS pid, server_stream.server_id AS sid
FROM suseServerAppstream server_stream
    INNER JOIN suseAppstream appstream ON appstream.name = server_stream.name
        AND appstream.arch = server_stream.arch
    INNER JOIN suseAppstreamApi api ON api.module_id = appstream.id
    INNER JOIN rhnPackageName pn ON pn.name = api.rpm
    INNER JOIN rhnPackage p ON p.name_id = pn.id
WHERE p.id NOT IN (
    SELECT package_id
    FROM suseServerAppStreamPackageView
    WHERE server_id = server_stream.server_id
);
