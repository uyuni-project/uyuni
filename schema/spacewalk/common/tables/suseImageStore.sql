--
-- Copyright (c) 2017 SUSE LLC
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

CREATE TABLE suseImageStore
(
    id             NUMBER NOT NULL
                     CONSTRAINT suse_imgstore_id_pk PRIMARY KEY,
    label          VARCHAR2(128) NOT NULL,
    uri            VARCHAR2(512) NOT NULL,
    store_type_id  NUMBER NOT NULL
                     CONSTRAINT suse_imgstore_type_fk
                       REFERENCES suseImageStoreType (id)
                       ON DELETE CASCADE,
    org_id         NUMBER NOT NULL
                     CONSTRAINT suse_imgstore_oid_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
    creds_id       NUMBER
                     CONSTRAINT suse_imgstore_creds_fk
                       REFERENCES suseCredentials (id)
                       ON DELETE SET NULL,
    created        timestamp with local time zone
                     DEFAULT (current_timestamp) NOT NULL,
    modified       timestamp with local time zone
                     DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_imgstore_oid_label_uq
    ON suseImageStore (org_id, label)
        TABLESPACE [[2m_tbs]];

CREATE SEQUENCE suse_imgstore_id_seq;
