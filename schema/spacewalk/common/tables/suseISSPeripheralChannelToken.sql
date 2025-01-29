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

CREATE TABLE suseISSPeripheralChannelToken
(
    id 	                BIGINT CONSTRAINT suse_issperipheralchanneltoken_id_pk PRIMARY KEY
        GENERATED ALWAYS AS IDENTITY,
    token               VARCHAR(1024) NOT NULL,
    peripheralchannel_id          BIGINT REFERENCES suseISSPeripheralChannels (id) UNIQUE NOT NULL,
    valid               BOOLEAN NOT NULL,
    expiration_date     TIMESTAMPTZ NULL
);