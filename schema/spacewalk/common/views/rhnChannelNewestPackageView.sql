--
-- Copyright (c) 2008--2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--
--
--
--
-- this is much more readable with ts=4, enjoy!

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
