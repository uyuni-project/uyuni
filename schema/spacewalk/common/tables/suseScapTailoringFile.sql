--
-- Copyright (c) 2025 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseScapTailoringFile
(
    id                   NUMERIC                                 NOT NULL
                         CONSTRAINT suseScapTailoringFile_pk PRIMARY KEY,
    name                 VARCHAR(255)                            NOT NULL,
    file_name            VARCHAR(255)                            NOT NULL,
    description          VARCHAR(4000),
    org_id               NUMERIC                                 NOT NULL
                         CONSTRAINT suse_trailingf_oid_fk
                           REFERENCES web_customer (id)
                           ON DELETE CASCADE,
    created              TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX suse_trailingf__oid_name_uq
    ON suseScapTailoringFile (org_id, name)
        ;
CREATE SEQUENCE suseScapTailoringFil_id_seq;
