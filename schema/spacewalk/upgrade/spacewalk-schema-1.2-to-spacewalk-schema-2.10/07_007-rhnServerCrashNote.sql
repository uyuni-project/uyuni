--
-- Copyright (c) 2013 Red Hat, Inc.
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


CREATE TABLE rhnServerCrashNote
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_server_crash_note_id_pk PRIMARY KEY
                   ,
    creator    NUMERIC
                   CONSTRAINT rhn_srv_crash_note_creator_fk
                       REFERENCES web_contact (id)
                       ON DELETE SET NULL,
    crash_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_srv_crash_note_sid_fk
                       REFERENCES rhnServerCrash (id)
                       ON DELETE CASCADE,
    subject    VARCHAR(80) NOT NULL,
    note       VARCHAR(4000),
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_srv_crash_note_sid_idx
    ON rhnServerCrashNote (crash_id)
    
    ;

CREATE INDEX rhn_srv_crash_note_creator_idx
    ON rhnServerCrashNote (creator)
    
    ;

CREATE SEQUENCE rhn_srv_crash_note_id_seq;
