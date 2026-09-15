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

CREATE TABLE IF NOT EXISTS suseISSHub
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

CREATE TABLE IF NOT EXISTS suseISSPeripheral
(
    id 	                BIGINT CONSTRAINT suse_issper_id_pk PRIMARY KEY
                          GENERATED ALWAYS AS IDENTITY,
    fqdn                VARCHAR(253) NOT NULL
                          CONSTRAINT suse_issper_fqdn_uq UNIQUE,
    root_ca             TEXT,
    mirror_creds_id     NUMERIC NULL
                          CONSTRAINT suse_issper_mirrcreds_fk
                            REFERENCES suseCredentials (id) ON DELETE SET NULL,
    created             TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
);

CREATE TABLE IF NOT EXISTS suseISSPeripheralChannels
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

CREATE UNIQUE INDEX IF NOT EXISTS suse_issperchan_pid_cid_uq
ON suseISSPeripheralChannels (peripheral_id, channel_id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'iss_access_token_type_t') THEN
        CREATE TYPE iss_access_token_type_t AS ENUM (
            'issued',
            'consumed'
        );
    ELSE
        RAISE NOTICE 'type "iss_access_token_type_t" already exists, skipping';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS suseISSAccessToken
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

CREATE UNIQUE INDEX IF NOT EXISTS suse_isstoken_server_fqdn_type_idx
    ON suseISSAccessToken (server_fqdn, type);

ALTER TABLE suseCredentials
    DROP CONSTRAINT rhn_type_ck;

ALTER TABLE suseCredentials
    ADD CONSTRAINT rhn_type_ck
    CHECK (type IN ('scc', 'vhm', 'registrycreds', 'cloudrmt', 'reportcreds', 'rhui', 'hub_scc'));

ALTER TABLE susecredentials
    DROP CONSTRAINT cred_type_check;

ALTER TABLE susecredentials
    ADD CONSTRAINT cred_type_check CHECK (
        CASE type
            WHEN 'scc' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'cloudrmt' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
                    and url is not null and url <> ''
            WHEN 'vhm' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'registrycreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'reportcreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'hub_scc' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
                    and url is not null and url <> ''
        END
    );
