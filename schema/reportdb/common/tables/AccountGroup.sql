--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE AccountGroup (
    mgm_id                      NUMERIC NOT NULL,
    account_id                  NUMERIC NOT NULL,
    account_group_id            NUMERIC NOT NULL,
    username                    VARCHAR(64),
    account_group_name          VARCHAR(64),
    account_group_type_id       NUMERIC,
    account_group_type_name     VARCHAR(64),
    account_group_type_label    VARCHAR(64),
    synced_date                 TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE AccountGroup
  ADD CONSTRAINT AccountGroup_pk PRIMARY KEY (mgm_id, account_id, account_group_id);
