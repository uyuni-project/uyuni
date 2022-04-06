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

CREATE TABLE ChannelErrata
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    errata_id                 NUMERIC NOT NULL,
    channel_label             VARCHAR(128),
    advisory_name             VARCHAR(100),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE ChannelErrata
  ADD CONSTRAINT ChannelErrata_pk PRIMARY KEY (mgm_id, channel_id, errata_id);
