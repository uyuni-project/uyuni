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

CREATE TABLE suseImageCustomDataValue
(
    id                NUMERIC NOT NULL
                          CONSTRAINT suse_icdv_id_pk PRIMARY KEY,
    image_info_id     NUMERIC NOT NULL
                          CONSTRAINT suse_icdv_prid_fk
                              REFERENCES suseImageInfo (id)
                              ON DELETE CASCADE,
    key_id            NUMERIC NOT NULL
                          CONSTRAINT suse_icdv_kid_fk
                              REFERENCES rhnCustomDataKey (id)
                              ON DELETE CASCADE,
    value             VARCHAR(4000),
    created_by        NUMERIC
                          CONSTRAINT suse_icdv_cb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    last_modified_by  NUMERIC
                          CONSTRAINT suse_icdv_lmb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_icdv_imgid_kid_uq
    ON suseImageCustomDataValue (image_info_id, key_id);

CREATE INDEX suse_icdv_kid_idx
    ON suseImageCustomDataValue (key_id);

CREATE SEQUENCE suse_icdv_id_seq;
