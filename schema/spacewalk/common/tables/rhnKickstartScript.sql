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


CREATE TABLE rhnKickstartScript
(
    id            NUMERIC NOT NULL,
    script_name   VARCHAR(40),
    kickstart_id  NUMERIC NOT NULL
                      CONSTRAINT rhn_ksscript_ksid_fk
                          REFERENCES rhnKSData (id)
                          ON DELETE CASCADE,
    position      NUMERIC NOT NULL,
    script_type   VARCHAR(4) NOT NULL
                      CONSTRAINT rhn_ksscript_st_ck
                          CHECK (script_type in ('pre','post')),
    chroot        CHAR(1)
                      DEFAULT ('Y') NOT NULL
                      CONSTRAINT rhn_ksscript_chroot_ck
                          CHECK (chroot in ('Y','N')),
    error_on_fail CHAR(1)
                      DEFAULT ('N') not NULL
                      CONSTRAINT rhn_ksscript_erroronfail_ck
                          CHECK (error_on_fail in ('Y','N')),
    raw_script    CHAR(1)
                      DEFAULT ('Y') NOT NULL
                      CONSTRAINT rhn_ksscript_rawscript_ck
                          CHECK (raw_script in ('Y','N')),
    interpreter   VARCHAR(80),
    data          BYTEA,
    created       TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ksscript_id_idx
    ON rhnKickstartScript (id)
    ;

CREATE INDEX rhn_ksscript_ksid_pos_idx
    ON rhnKickstartScript (kickstart_id, position)
    ;

CREATE SEQUENCE rhn_ksscript_id_seq;

ALTER TABLE rhnKickstartScript
    ADD CONSTRAINT rhn_ksscript_id_pk PRIMARY KEY (id);

ALTER TABLE rhnKickstartScript
    ADD CONSTRAINT rhn_ksscript_ksid_pos_uq UNIQUE (kickstart_id, position);

