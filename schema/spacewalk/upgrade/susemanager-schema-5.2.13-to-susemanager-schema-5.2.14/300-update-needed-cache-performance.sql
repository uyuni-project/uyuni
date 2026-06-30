--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Replace the MATERIALIZED CTE in update_needed_cache with a temp table
-- to give the planner accurate row estimates and enable a fast indexed anti-join.
-- See bsc#1267912 for details.
create or replace function rhn_server.update_needed_cache(
    server_id_in in numeric
) returns void as $$
    begin
      delete from rhnServerNeededCache
        where server_id = server_id_in;
      create temp table if not exists tmp_hidden_packages on commit drop as
        select pid from suseServerAppStreamHiddenPackagesView where sid = server_id_in
        with no data;
      insert into tmp_hidden_packages
        select pid from suseServerAppStreamHiddenPackagesView where sid = server_id_in;
      analyze tmp_hidden_packages;
      create index on tmp_hidden_packages(pid);

      insert into rhnServerNeededCache
             (server_id, errata_id, package_id, channel_id)
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
          and (x.errata_id IS NULL or e.advisory_status != 'retracted')
          and NOT EXISTS (SELECT 1 FROM tmp_hidden_packages WHERE pid = p.id);
      drop table tmp_hidden_packages;
	end$$ language plpgsql;
