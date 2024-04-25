--
-- Copyright (c) 2021 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

DROP VIEW IF EXISTS rhnChannelNewestPackageView;
CREATE VIEW rhnChannelNewestPackageView AS
SELECT channel_id,
       name_id,
       evr_id,
       package_arch_id,
       max(package_id) as package_id
FROM (
       SELECT m.channel_id          as channel_id,
              p.name_id             as name_id,
              p.evr_id              as evr_id,
              m.package_arch_id     as package_arch_id,
              p.id                  as package_id
         FROM
              (select max(pe.evr) AS max_evr,
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
group by channel_id, name_id, evr_id, package_arch_id
;

