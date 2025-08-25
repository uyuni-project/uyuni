CREATE INDEX IF NOT EXISTS suse_srvappstream_nsca_sid_idx ON suseServerAppstream (name, stream, arch, context, server_id);
CREATE INDEX IF NOT EXISTS suse_srvappstream_nsa_sid_idx  ON suseServerAppstream (name, stream, arch, server_id);
CREATE INDEX IF NOT EXISTS suse_appstream_nsca_idx        ON suseAppStream       (name, stream, arch, context);


CREATE OR REPLACE VIEW suseServerAppStreamPackageView AS
SELECT
    p.package_id,
    c.id AS channel_id,
    c.label AS channel_label,
    s.server_id
FROM suseAppstream m
JOIN suseAppstreamPackage p ON p.module_id = m.id
JOIN rhnchannel c ON m.channel_id = c.id
Join rhnServerChannel sc ON c.id = sc.channel_id
JOIN suseServerAppstream s
    ON  m.name = s.name
    AND s.server_id = sc.server_id
    AND m.stream = s.stream
    AND m.arch = s.arch;


CREATE OR REPLACE VIEW suseServerAppStreamHiddenPackagesView AS

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


create or replace function rhn_server.update_needed_cache(
    server_id_in in numeric
) returns void as $$
    begin
      delete from rhnServerNeededCache
        where server_id = server_id_in;
      insert into rhnServerNeededCache
             (server_id, errata_id, package_id, channel_id)
        (
		   with hidden_packages as materialized (
              select pid from suseServerAppStreamHiddenPackagesView where sid = server_id_in
           )
		   select distinct sp.server_id, x.errata_id, p.id, x.channel_id
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
            and (x.errata_id IS NULL or e.advisory_status != 'retracted') -- packages which are part of a retracted errata should not be installed
            and NOT EXISTS (SELECT 1 FROM hidden_packages WHERE pid = p.id));
	end$$ language plpgsql;
