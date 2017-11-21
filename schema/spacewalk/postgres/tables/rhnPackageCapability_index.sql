-- oracle equivalent source sha1 e8780ef427c5ee71a056b15d52a3a5bcfd4df75f
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE UNIQUE INDEX rhn_pkg_cap_name_version_uq
    ON rhnPackageCapability (name, version)
 where version is not null;

create unique index rhn_pkg_cap_name_uq
    on rhnPackageCapability (name)
 where version is null;
