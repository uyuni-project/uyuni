--
-- Copyright (c) 2023 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseImageSyncProject
(
    id                   NUMERIC NOT NULL
                           CONSTRAINT suse_imgsync_prj_pk PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    org_id               NUMERIC NOT NULL
                           CONSTRAINT suse_imgsync_prj_org_fk
                             REFERENCES web_customer (id),
    dest_store_id        NUMERIC NOT NULL
                           CONSTRAINT suse_imgsync_prj_dsid_fk
                             REFERENCES suseImageStore (id)
                             ON DELETE CASCADE,
    scoped               BOOLEAN NOT NULL DEFAULT (true),
    created              TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_imgsync_prj_id_seq;

CREATE UNIQUE INDEX suse_imgsync_prj_name_uq
  ON suseImageSyncProject (org_id, name);
