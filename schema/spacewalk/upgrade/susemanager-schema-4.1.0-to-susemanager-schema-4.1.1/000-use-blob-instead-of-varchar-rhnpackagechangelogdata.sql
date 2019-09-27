--
-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

DROP VIEW rhnPackageChangeLog;

ALTER TABLE rhnPackageChangeLogData ALTER COLUMN text TYPE TEXT;

CREATE VIEW rhnPackageChangeLog
AS
SELECT rhnPackageChangeLogRec.id,
       rhnPackageChangeLogRec.package_id,
       rhnPackageChangeLogRec.changelog_data_id,
       rhnPackageChangeLogData.name,
       rhnPackageChangeLogData.text,
       rhnPackageChangeLogData.time,
       rhnPackageChangeLogRec.created,
       rhnPackageChangeLogRec.modified
FROM rhnPackageChangeLogRec, rhnPackageChangeLogData
WHERE rhnPackageChangeLogRec.changelog_data_id = rhnPackageChangeLogData.id;
