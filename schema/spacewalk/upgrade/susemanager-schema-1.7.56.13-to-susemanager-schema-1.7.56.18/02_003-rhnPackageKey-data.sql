--
-- Copyright (c) 2012 Novell, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
--

update rhnPackageKey set provider_id = lookup_package_provider('openSUSE')
 where key_id = 'b88b2fd43dbdc284';
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
select sequence_nextval('rhn_pkey_id_seq'), 'b88b2fd43dbdc284', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE')
from dual
where not exists ( select 1 from rhnPackageKey where key_id = 'b88b2fd43dbdc284' );


commit;

