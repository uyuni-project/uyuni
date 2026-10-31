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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseProfileCustomDataValue
(
    id                NUMERIC NOT NULL
                          CONSTRAINT suse_pcdv_id_pk PRIMARY KEY,
    profile_id        NUMERIC NOT NULL
                          CONSTRAINT suse_pcdv_prid_fk
                              REFERENCES suseImageProfile (profile_id)
                              ON DELETE CASCADE,
    key_id            NUMERIC NOT NULL
                          CONSTRAINT suse_pcdv_kid_fk
                              REFERENCES rhnCustomDataKey (id)
                              ON DELETE CASCADE,
    value             VARCHAR(4000),
    created_by        NUMERIC
                          CONSTRAINT suse_pcdv_cb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    last_modified_by  NUMERIC
                          CONSTRAINT suse_pcdv_lmb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_pcdv_prid_kid_uq
    ON suseProfileCustomDataValue (profile_id, key_id);

CREATE INDEX suse_pcdv_kid_idx
    ON suseProfileCustomDataValue (key_id);

CREATE SEQUENCE suse_pcdv_id_seq;
