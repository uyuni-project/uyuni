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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnWebContactChangeLog
(
    id                   NUMERIC
                             CONSTRAINT rhn_wcon_cl_id_pk PRIMARY KEY,
    web_contact_id       NUMERIC NOT NULL
                             CONSTRAINT rhn_wcon_cl_wcon_id_fk
                                 REFERENCES web_contact (id)
                                 ON DELETE CASCADE,
    web_contact_from_id  NUMERIC
                             CONSTRAINT rhn_wcon_cl_wcon_from_id_fk
                                 REFERENCES web_contact (id)
                                 ON DELETE SET NULL,
    change_state_id      NUMERIC NOT NULL
                             CONSTRAINT rhn_wcon_cl_csid_fk
                                 REFERENCES rhnWebContactChangeState (id),
    date_completed       TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_wcon_disabled_wcon_id_idx
    ON rhnWebContactChangeLog (web_contact_id)
    ;

CREATE SEQUENCE rhn_wcon_disabled_seq;

