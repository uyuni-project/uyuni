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

CREATE TABLE ChannelPackage
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    package_id                NUMERIC NOT NULL,
    channel_label             VARCHAR(128),
    package_name              VARCHAR(256),
    package_epoch             VARCHAR(16),
    package_version           VARCHAR(512),
    package_release           VARCHAR(512),
    package_type              VARCHAR(10),
    package_arch              VARCHAR(64),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE ChannelPackage
  ADD CONSTRAINT ChannelPackage_pk PRIMARY KEY (mgm_id, channel_id, package_id);
