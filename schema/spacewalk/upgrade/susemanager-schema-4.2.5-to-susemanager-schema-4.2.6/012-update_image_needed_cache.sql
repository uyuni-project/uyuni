--
-- Copyright (c) 2017 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

create or replace function
update_image_needed_cache(image_id_in in numeric)
returns void as $$
declare
  update_lock numeric;
begin
  select id into update_lock from suseImageInfo where id = image_id_in for update;
  delete from rhnImageNeededCache
   where image_id = image_id_in;
  insert into rhnImageNeededCache
         (image_id, errata_id, package_id, channel_id)
    (select distinct ip.image_info_id, x.errata_id, p.id, x.channel_id
       FROM (SELECT ip_ip.image_info_id, ip_ip.name_id,
                    ip_ip.package_arch_id, max(ip_pe.evr) AS max_evr
               FROM suseImageInfoPackage ip_ip
               join rhnPackageEvr ip_pe ON ip_pe.id = ip_ip.evr_id
              GROUP BY ip_ip.image_info_id, ip_ip.name_id, ip_ip.package_arch_id) ip
       join rhnPackage p ON p.name_id = ip.name_id
       join rhnPackageEvr pe ON pe.id = p.evr_id
                AND (ip.max_evr).type = (pe.evr).type AND ip.max_evr < pe.evr
       join rhnPackageUpgradeArchCompat puac
                ON puac.package_arch_id = ip.package_arch_id
                AND puac.package_upgrade_arch_id = p.package_arch_id
       join suseImageInfoChannel ic ON ic.image_info_id = ip.image_info_id
       join rhnChannelPackage cp ON cp.package_id = p.id
                AND cp.channel_id = ic.channel_id
       left join (SELECT ep.errata_id, ce.channel_id, ep.package_id
                    FROM rhnChannelErrata ce
                    join rhnErrataPackage ep
                             ON ep.errata_id = ce.errata_id
                    join suseImageInfoChannel ic_ic
                             ON ic_ic.channel_id = ce.channel_id
                   WHERE ic_ic.image_info_id = image_id_in) x
         ON x.channel_id = ic.channel_id
                AND x.package_id = cp.package_id
       left join rhnErrata e on x.errata_id = e.id
      where ip.image_info_id = image_id_in
        and (x.errata_id IS NULL or e.advisory_status != 'retracted')); -- packages which are part of a retracted errata should not be installed
end;
$$
language plpgsql;
