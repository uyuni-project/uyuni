--
-- Copyright (c) 2023 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
INSERT INTO suseInternalState (id, name, label)
         VALUES (1, 'certs', 'Certificates');

INSERT INTO suseInternalState (id, name, label)
         VALUES (2, 'channels', 'Channels');

INSERT INTO suseInternalState (id, name, label)
         VALUES (3, 'hardware.profileupdate', 'Hardware Profile Update');

INSERT INTO suseInternalState (id, name, label)
         VALUES (4, 'packages', 'Packages');

INSERT INTO suseInternalState (id, name, label)
         VALUES (5, 'packages.profileupdate', 'Package Profile Update');

INSERT INTO suseInternalState (id, name, label)
         VALUES (6, 'uptodate', 'Update System');

INSERT INTO suseInternalState (id, name, label)
         VALUES (7, 'util.syncbeacons', 'Sync Beacons');

INSERT INTO suseInternalState (id, name, label)
         VALUES (8, 'util.syncall', 'Sync All');

INSERT INTO suseInternalState (id, name, label)
         VALUES (9, 'util.syncgrains', 'Sync Grains');

INSERT INTO suseInternalState (id, name, label)
         VALUES (10, 'util.syncmodules', 'Sync Modules');

INSERT INTO suseInternalState (id, name, label)
         VALUES (11, 'util.syncstates', 'Sync States');

INSERT INTO suseInternalState (id, name, label)
         VALUES (12, 'update-salt', 'Update Salt');

INSERT INTO suseInternalState (id, name, label)
         VALUES (13, 'reboot', 'Reboot system');

INSERT INTO suseInternalState (id, name, label)
         VALUES (14, 'rebootifneeded', 'Reboot system if needed');

INSERT INTO suseInternalState (id, name, label)
         VALUES (15, 'uptimetracker.requestdata', 'Uptime Tracking Data');

