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

CREATE INDEX IF NOT EXISTS suse_appstream_name_stream_arch_idx
  ON suseAppStream (name, stream, arch);

CREATE INDEX IF NOT EXISTS suse_srvappstream_name_stream_arch_idx
  ON suseServerAppStream (name, stream, arch);

CREATE INDEX IF NOT EXISTS suse_appstream_api_rpm_idx
  ON suseAppStreamApi (rpm);
