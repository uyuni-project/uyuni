--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

-- ANALYZE heavily underestimates the distinct package_id values of these
-- tables because rows are clustered by package. The planner then expects
-- thousands of rows per package and chooses parallel plans whose worker
-- startup dominates the per-package queries of the repodata generation.
ALTER TABLE rhnPackageFile ALTER COLUMN package_id SET (n_distinct = -0.004);
ALTER TABLE rhnPackageProvides ALTER COLUMN package_id SET (n_distinct = -0.01);
ALTER TABLE rhnPackageChangeLogRec ALTER COLUMN package_id SET (n_distinct = -0.004);

ANALYZE rhnPackageFile;
ANALYZE rhnPackageProvides;
ANALYZE rhnPackageChangeLogRec;
