-- oracle equivalent source sha1 5b15cd99c4f969af0a17f7a91411727a91cb33bd
--
--
-- Copyright (c) 2018 SUSE LLC
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

CREATE TABLE IF NOT EXISTS suseContentProject(
    id          NUMERIC NOT NULL
                    CONSTRAINT suse_ct_project_id_pk PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    org_id      NUMERIC NOT NULL
                    CONSTRAINT suse_ct_project_oid_fk
                    REFERENCES web_customer(id)
                    ON DELETE CASCADE,
    label       VARCHAR(24) NOT NULL,
    description TEXT,
    first_env_id NUMERIC,
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)

;

CREATE SEQUENCE IF NOT EXISTS suse_ct_project_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_ct_project_lblid_uq
    ON suseContentProject(label);

CREATE UNIQUE INDEX IF NOT EXISTS suse_ct_prj_nameid_oid_uq
    ON suseContentProject(org_id, name);

