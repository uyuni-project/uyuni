-- drop the dependent views
DROP VIEW IF EXISTS suseserverappstreamhiddenpackagesview;
DROP VIEW IF EXISTS suseserverappstreampackageview;

-- alter tables to increase field size
ALTER TABLE suseappstream
ALTER COLUMN arch TYPE varchar(64),
ALTER COLUMN context TYPE varchar(128);

ALTER TABLE suseserverappstream
ALTER COLUMN arch TYPE varchar(64),
ALTER COLUMN context TYPE varchar(128);

-- recreate the dependent views
CREATE OR REPLACE VIEW suseServerAppStreamPackageView AS
SELECT
    p.package_id,
    c.id AS channel_id,
    c.label AS channel_label,
    s.server_id
FROM suseAppstream m
JOIN suseAppstreamPackage p ON p.module_id = m.id
JOIN rhnchannel c ON m.channel_id = c.id
JOIN rhnServerChannel sc ON c.id = sc.channel_id
JOIN suseServerAppstream s
    ON  m.name = s.name
    AND s.server_id = sc.server_id
    AND m.stream = s.stream
    AND m.arch = s.arch;


CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS

-- If a package is part of any appstream,
-- and this appstream is not enabled in
-- a server, it should appear here.
SELECT sasp.package_id AS pid, sc.server_id AS sid
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

UNION ALL

-- If a package is part of an enabled appstream, all the packages
-- whose name matches with appstream api need to be filtered out
-- except the packages that are part of the enabled appstream.
SELECT p.id AS pid, server_stream.server_id AS sid
FROM suseServerAppstream server_stream
    INNER JOIN suseAppstream appstream ON appstream.name = server_stream.name
        AND appstream.stream = server_stream.stream
        AND appstream.arch = server_stream.arch
    INNER JOIN suseAppstreamApi api ON api.module_id = appstream.id
    INNER JOIN rhnPackageName pn ON pn.name = api.rpm
    INNER JOIN rhnPackage p ON p.name_id = pn.id
    INNER JOIN rhnChannelPackage cp ON cp.package_id = p.id
    INNER JOIN rhnServerChannel sc ON sc.channel_id = cp.channel_id
    	AND sc.server_id = server_stream.server_id
WHERE NOT EXISTS (
    SELECT package_id
    FROM suseServerAppStreamPackageView
    WHERE server_id = server_stream.server_id
        AND package_id = p.id
);
