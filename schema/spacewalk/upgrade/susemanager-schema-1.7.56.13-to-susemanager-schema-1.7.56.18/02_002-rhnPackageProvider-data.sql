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
insert into rhnPackageProvider (id, name) values
(sequence_nextval('rhn_package_provider_id_seq'), 'openSUSE' );

commit;

