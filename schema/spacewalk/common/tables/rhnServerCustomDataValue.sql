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


CREATE TABLE rhnServerCustomDataValue
(
    server_id         NUMERIC NOT NULL
                          CONSTRAINT rhn_scdv_sid_fk
                              REFERENCES rhnServer (id),
    key_id            NUMERIC NOT NULL
                          CONSTRAINT rhn_scdv_kid_fk
                              REFERENCES rhnCustomDataKey (id),
    value             VARCHAR(4000),
    created_by        NUMERIC
                          CONSTRAINT rhn_scdv_cb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    last_modified_by  NUMERIC
                          CONSTRAINT rhn_scdv_lmb_fk
                              REFERENCES web_contact (id)
                              ON DELETE SET NULL,
    created           TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL,
    modified          TIMESTAMPTZ
                          DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_scdv_sid_kid_uq
    ON rhnServerCustomDataValue (server_id, key_id);

CREATE INDEX rhn_scdv_kid_idx
    ON rhnServerCustomDataValue (key_id);

