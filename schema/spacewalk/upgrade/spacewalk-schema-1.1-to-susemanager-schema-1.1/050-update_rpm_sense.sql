--
-- Copyright (c) 2010 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

UPDATE "rhnPackageSense" set LABEL = 'RPMSENSE_MISSINGOK' where ID = 524288;
INSERT into "rhnPackageSense" (ID,LABEL) values (67108864,'RPMSENSE_KEYRING');
INSERT into "rhnPackageSense" (ID,LABEL) values (134217728,'RPMSENSE_STRONG');
INSERT into "rhnPackageSense" (ID,LABEL) values (268435456,'RPMSENSE_CONFIG');

