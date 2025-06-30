CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS

SELECT DISTINCT sasp.package_id AS pid, sc.server_id AS sid
FROM rhnServerChannel sc
    INNER JOIN suseAppStream sas ON sas.channel_id = sc.channel_id
    INNER JOIN suseAppStreamPackage sasp ON sasp.module_id = sas.id
WHERE NOT EXISTS (
    SELECT 1
    FROM suseServerAppstream ssa
    WHERE ssa.name = sas.name
        AND ssa.stream = sas.stream
        AND ssa.context = sas.context
        AND ssa.arch = sas.arch
        AND ssa.server_id = sc.server_id
)

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

CREATE INDEX IF NOT EXISTS suse_appstream_pkg_module_id_idx ON suseAppstreamPackage(module_id);
