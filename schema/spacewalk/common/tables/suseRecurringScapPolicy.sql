--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
CREATE TABLE suseRecurringScapPolicy
(
  rec_id            NUMERIC NOT NULL PRIMARY KEY,
  test_mode         CHAR(1) NOT NULL DEFAULT 'N',
  
  scap_policy_id    INTEGER,
  
  -- Foreign Key references suseRecurringAction
  FOREIGN KEY (rec_id) REFERENCES suseRecurringAction(id) ON DELETE CASCADE,
  
  -- Foreign Key references suseScapPolicy
  FOREIGN KEY (scap_policy_id) REFERENCES suseScapPolicy(id) ON DELETE CASCADE
);