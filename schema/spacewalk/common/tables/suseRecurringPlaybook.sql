--
-- Copyright (c) 2025 SUSE LLC
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
CREATE TABLE suseRecurringPlaybook
(
  rec_id            NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_play_id_pk PRIMARY KEY
                    CONSTRAINT suse_recurring_action_play_id_fk
                      REFERENCES suseRecurringAction(id)
                      ON DELETE CASCADE,
  extra_vars        BYTEA,
  flush_cache       CHAR(1) NOT NULL
                    DEFAULT 'N',
  inventory_path    VARCHAR(1024),
  playbook_path     VARCHAR(1024) NOT NULL,
  test_mode         CHAR(1) NOT NULL
                    DEFAULT 'N'
);
