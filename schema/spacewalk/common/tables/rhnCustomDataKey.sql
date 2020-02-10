--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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


CREATE TABLE rhnCustomDataKey
(
    id                NUMERIC NOT NULL
                          CONSTRAINT rhn_cdatakey_pk PRIMARY KEY,
    org_id            NUMERIC NOT NULL
                          CONSTRAINT rhn_cdatakey_oid_fk
                              REFERENCES web_customer (id)
                              ON DELETE CASCADE,
    label             VARCHAR(64) NOT NULL,
    description       VARCHAR(4000) NOT NULL,
    created_by        NUMERIC
                          CONSTRAINT rhn_cdatakey_cb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    last_modified_by  NUMERIC
                          CONSTRAINT rhn_cdatakey_lmb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_cdatakey_oid_label_id_idx
    ON rhnCustomDataKey (org_id, label, id)
    ;

CREATE SEQUENCE rhn_cdatakey_id_seq;

ALTER TABLE rhnCustomDataKey
    ADD CONSTRAINT rhn_cdatakey_oid_label_uq UNIQUE (org_id, label);

