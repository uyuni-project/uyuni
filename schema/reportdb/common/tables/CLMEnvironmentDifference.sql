--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

CREATE TABLE CLMEnvironmentDifference
(
    mgm_id              NUMERIC NOT NULL,
    diff_id             BIGINT NOT NULL,
    project_label       VARCHAR(24) NOT NULL,
    environment_label   VARCHAR(16) NOT NULL,
    channel_id          NUMERIC NOT NULL,
    diff_action         VARCHAR(8) NOT NULL, -- + or - or x
    entry_id            NUMERIC NOT NULL,
    entry_type          VARCHAR(16) NOT NULL, -- PACKAGE, ERRATA
    entry_name          VARCHAR(256) NOT NULL,
    entry_description   VARCHAR NOT NULL,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE CLMEnvironmentDifference
  ADD CONSTRAINT CLMEnvironmentDifference_pk PRIMARY KEY (mgm_id, diff_id);

CREATE INDEX IF NOT EXISTS CLMEnvironmentDifference_eidt_idx
  ON CLMEnvironmentDifference (entry_id, entry_type);

CREATE INDEX IF NOT EXISTS CLMEnvironmentDifference_ped_idx
  ON CLMEnvironmentDifference (project_label, environment_label);
