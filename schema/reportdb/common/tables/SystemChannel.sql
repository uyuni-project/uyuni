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

CREATE TABLE SystemChannel
(
    mgm_id                      NUMERIC NOT NULL,
    system_id                   NUMERIC NOT NULL,
    channel_id                  NUMERIC NOT NULL,
    name                        VARCHAR(256),
    description                 VARCHAR(4000),
    architecture_name           VARCHAR(64),
    product_name                VARCHAR(128),
    parent_channel_id           NUMERIC,
    parent_channel_name         VARCHAR(256),
    synced_date                 TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemChannel
  ADD CONSTRAINT SystemChannel_pk PRIMARY KEY (mgm_id, system_id, channel_id);
