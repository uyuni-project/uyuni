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
         VALUES (3, 'hardware.profileupdate', 'Hardware Profileupdate');

INSERT INTO suseInternalState (id, name, label)
         VALUES (4, 'packages', 'Packages');

INSERT INTO suseInternalState (id, name, label)
         VALUES (5, 'packages.profileupdate', 'Package Profileupdate');

INSERT INTO suseInternalState (id, name, label)
         VALUES (6, 'util.syncbeacons', 'Sync Beacons');

INSERT INTO suseInternalState (id, name, label)
         VALUES (7, 'util.synccustomall', 'Sync Custom');

INSERT INTO suseInternalState (id, name, label)
         VALUES (8, 'util.syncgrains', 'Sync Grains');

INSERT INTO suseInternalState (id, name, label)
         VALUES (9, 'util.syncmodules', 'Sync Modules');

INSERT INTO suseInternalState (id, name, label)
         VALUES (10, 'util.syncstates', 'Sync States');
