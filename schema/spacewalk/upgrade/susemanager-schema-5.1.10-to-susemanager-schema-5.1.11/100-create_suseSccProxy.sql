--
-- Copyright (c) 2025 SUSE LLC
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'scc_proxy_status_t') THEN
        CREATE TYPE scc_proxy_status_t AS ENUM (
            'scc_creation_pending',
            'scc_created',
            'scc_removal_pending',
            'scc_virthost_pending'
        );
    ELSE
        RAISE NOTICE 'type "scc_proxy_status_t" already exists, skipping';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS suseSccProxy
(
    proxy_id            BIGINT CONSTRAINT suse_sccproxy_proxy_id_pk PRIMARY KEY
                                                  GENERATED ALWAYS AS IDENTITY,
    peripheral_fqdn     VARCHAR(128),
    scc_login           VARCHAR(64),
    scc_passwd          VARCHAR(64),
    scc_creation_json   TEXT,
    scc_id              NUMERIC,
    status              scc_proxy_status_t NOT NULL,
    scc_regerror_timestamp TIMESTAMPTZ,

    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
);

