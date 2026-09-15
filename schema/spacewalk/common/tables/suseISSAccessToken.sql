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

CREATE TABLE suseISSAccessToken
(
    id 	                BIGINT CONSTRAINT suse_isstoken_id_pk PRIMARY KEY
                          GENERATED ALWAYS AS IDENTITY,
    token               VARCHAR(1024) NOT NULL,
    type                iss_access_token_type_t NOT NULL,
    server_fqdn         VARCHAR(512) NOT NULL,
    valid               BOOLEAN,
    expiration_date     TIMESTAMPTZ NULL,
    created             TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX suse_isstoken_server_fqdn_type_idx
    ON suseISSAccessToken (server_fqdn, type);
