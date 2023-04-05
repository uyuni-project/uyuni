--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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
-- $Id$
--

alter trigger rhn_package_mod_trig disable;
alter TABLE rhnPackage add (compat_new number(1) default 0);
update rhnPackage set compat_new = compat;
alter trigger rhn_package_mod_trig enable;
alter TABLE rhnPackage drop column compat;
alter TABLE rhnPackage rename column compat_new to compat;
show errors
