--
-- Copyright (c) 2024 SUSE LLC
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
CREATE OR REPLACE VIEW suseServerPackagesDisabledModulesView AS
select distinct sasp.package_id as pid, sc.server_id as sid
from rhnserverchannel sc
inner join suseappstream sas
on sas.channel_id = sc.channel_id
inner join suseappstreampackage sasp
on sasp.module_id = sas.id
left join suseserverappstream ssa
    on ssa.name = sas.name
    and ssa.stream = sas.stream
    and sas.version = ssa.version
    and sas.context = ssa.context
    and sas.arch = ssa.arch
where ssa.id is null;
