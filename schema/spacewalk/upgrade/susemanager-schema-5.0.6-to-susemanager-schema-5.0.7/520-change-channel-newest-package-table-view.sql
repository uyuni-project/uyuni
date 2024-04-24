ALTER TABLE rhnChannelNewestPackage
  ADD COLUMN IF NOT EXISTS appstream_id NUMERIC;

ALTER TABLE rhnChannelNewestPackage DROP CONSTRAINT IF EXISTS rhn_cnp_aid_fk;

ALTER TABLE rhnChannelNewestPackage ADD CONSTRAINT rhn_cnp_aid_fk FOREIGN KEY (appstream_id) REFERENCES suseappstream(id) ON DELETE CASCADE;

ALTER TABLE rhnChannelNewestPackage DROP CONSTRAINT IF EXISTS rhn_cnp_cid_nid_uq;

DROP INDEX IF EXISTS rhn_cnp_cid_nid_aid_uq;

DROP INDEX IF EXISTS rhn_cnp_cid_nid_uq;

CREATE UNIQUE INDEX rhn_cnp_cid_nid_aid_uq
    ON rhnchannelnewestpackage (channel_id, name_id, package_arch_id, appstream_id)
    WHERE appstream_id IS NOT NULL;

CREATE UNIQUE INDEX rhn_cnp_cid_nid_uq
    ON rhnchannelnewestpackage (channel_id, name_id, package_arch_id)
    WHERE appstream_id IS NULL;

CREATE OR REPLACE VIEW rhnChannelNewestPackageView AS
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
            SELECT m.channel_id          as channel_id,
                   p.name_id             as name_id,
                   p.evr_id              as evr_id,
                   m.package_arch_id     as package_arch_id,
                   p.id                  as package_id,
                   p.build_time          as build_time,
                   m.appstream_id        as appstream_id
            FROM (select max(pe.evr) AS max_evr,
			  cp.channel_id,
			  p.name_id,
			  p.package_arch_id,
			  appstream.id AS appstream_id
			from rhnPackageEVR                               pe
			inner join susePackageExcludingPartOfPtf         p
			    on p.evr_id = pe.id
			inner join suseChannelPackageRetractedStatusView cp
			    on cp.package_id = p.id
			left join suseAppstreamPackage                   appstreampkg
			    on appstreampkg.package_id = p.id
			left join suseAppstream                          appstream
			    on appstream.channel_id = cp.channel_id and appstream.id = appstreampkg.module_id
			where NOT cp.is_retracted
			group by cp.channel_id, p.name_id, p.package_arch_id, appstream.id) m,
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
      group by channel_id, name_id, evr_id, package_arch_id, build_time, appstream_id
) n
WHERE rn = 1;
