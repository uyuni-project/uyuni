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
-- SPDX-License-Identifier: GPL-2.0-only
--

CREATE TABLE suseISSPeripheralChannels
(
    id                          BIGINT CONSTRAINT suse_issperchan_id_pk PRIMARY KEY
                                  GENERATED ALWAYS AS IDENTITY,
    peripheral_id               BIGINT NOT NULL
                                  CONSTRAINT suse_issperchan_pid_fk
                                    REFERENCES suseISSPeripheral(id) ON DELETE CASCADE,
    channel_id                  NUMERIC NOT NULL
                                  CONSTRAINT suse_issperchan_cid_fk
                                    REFERENCES rhnChannel(id)  ON DELETE CASCADE,
    peripheral_org_id           INTEGER NULL,
    created                     TIMESTAMPTZ
                                  DEFAULT (current_timestamp) NOT NULL,
    modified                    TIMESTAMPTZ
                                  DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX use_issperchan_pid_cid_uq
ON suseISSPeripheralChannels (peripheral_id, channel_id);
