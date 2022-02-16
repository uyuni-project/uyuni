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

CREATE TABLE SystemAction
(
    mgm_id              NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    action_id           NUMERIC NOT NULL,
    hostname            VARCHAR(128),
    pickup_time         TIMESTAMPTZ,
    completion_time     TIMESTAMPTZ,
    status              VARCHAR(16),
    event               VARCHAR(100),
    event_data          TEXT,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemAction
  ADD CONSTRAINT SystemAction_pk PRIMARY KEY (mgm_id, system_id, action_id);
