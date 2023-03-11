--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Drop column 'image_type'
ALTER TABLE rhnActionImageDeploy DROP COLUMN image_type;

-- Allow NULL values in column 'bridge_device'
ALTER TABLE rhnActionImageDeploy MODIFY bridge_device VARCHAR2(32) NULL;

