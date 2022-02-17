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

CREATE TABLE Channel
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    name                      VARCHAR(256),
    label                     VARCHAR(128),
    type                      VARCHAR(50),
    arch                      VARCHAR(64),
    summary                   VARCHAR(500),
    description               VARCHAR(4000),
    parent_channel_label      VARCHAR(128),
    organization              VARCHAR(128),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE Channel
  ADD CONSTRAINT Channel_pk PRIMARY KEY (mgm_id, channel_id);
