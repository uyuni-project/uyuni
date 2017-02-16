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
--
create or replace view
rhnImageNeededPackageCache
(
    image_id,
    package_id,
    errata_id
)
as
select
	image_id,
	package_id,
	max(errata_id) as errata_id
	from rhnImageNeededCache
	group by image_id, package_id;
