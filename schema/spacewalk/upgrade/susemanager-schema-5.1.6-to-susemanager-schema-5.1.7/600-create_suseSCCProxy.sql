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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE IF NOT EXISTS suseSCCproxy
(
    proxy_id            NUMERIC
                        CONSTRAINT suse_sccproxy_proxy_id_pk
                        PRIMARY KEY,
    peripheral_fqdn     VARCHAR(128),
    scc_login           VARCHAR(64),
    scc_passwd          VARCHAR(64),
    scc_creation_json   TEXT,
    scc_id              NUMERIC,
    status              VARCHAR(32),
    scc_regerror_timestamp TIMESTAMPTZ,

    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
);


CREATE SEQUENCE IF NOT EXISTS suse_sccproxy_id_seq
    START 100000
    MINVALUE 100000;
