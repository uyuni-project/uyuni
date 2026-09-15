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

CREATE TABLE suseISSHub
(
    id 	                BIGINT CONSTRAINT suse_iss_hub_id_pk PRIMARY KEY
                          GENERATED ALWAYS AS IDENTITY,
    fqdn                VARCHAR(253) NOT NULL
                          CONSTRAINT suse_iss_hub_fqdn_uq UNIQUE,
    root_ca             TEXT,
    gpg_key             TEXT,
    mirror_creds_id     NUMERIC NULL
                        CONSTRAINT suse_iss_hub_mirrcreds_fk
                          REFERENCES suseCredentials (id) ON DELETE SET NULL,
    created             TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
);
