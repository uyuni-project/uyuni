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

create or replace view
suseImageOverview
(
    org_id,
    image_id,
    image_name,
    image_version,
    image_checksum,
    modified,
    image_arch_name,
    action_id,
    profile_id,
    store_id,
    build_server_id,
    security_errata,
    bug_errata,
    enhancement_errata,
    outdated_packages
)
as
select
    i.org_id, i.id, i.name, i.version, i.checksum, i.modified,
    ( select name from rhnServerArch where id = i.image_arch_id),
    i.action_id, i.profile_id, i.store_id, i.build_server_id,
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Security Advisory'),
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Bug Fix Advisory'),
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Product Enhancement Advisory'),
    ( select count(distinct p.name_id) from rhnPackage p, rhnImageNeededPackageCache inpc
      where
             inpc.image_id = i.id
	 and p.id = inpc.package_id
	 )
from
    suseImageInfo i
;
