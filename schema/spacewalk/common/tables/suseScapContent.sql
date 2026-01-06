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

CREATE TABLE suseScapContent
(
    id            NUMERIC NOT NULL
                  CONSTRAINT suse_scap_content_id_pk PRIMARY KEY,
    org_id        NUMERIC NOT NULL
                  CONSTRAINT suse_scap_content_oid_fk
                  REFERENCES web_customer (id)
                  ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    description   VARCHAR(4000),
    file_name     VARCHAR(4000) NOT NULL,
    created       TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX suse_scap_content_oid_name_uq
    ON suseScapContent (org_id, name);

CREATE SEQUENCE suse_scap_content_id_seq;
