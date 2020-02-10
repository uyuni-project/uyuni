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


CREATE TABLE rhnActionConfigDate
(
    action_id        NUMERIC NOT NULL
                         CONSTRAINT rhn_actioncd_aid_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    start_date       TIMESTAMPTZ NOT NULL,
    end_date         TIMESTAMPTZ,
    import_contents  CHAR(1) NOT NULL
                         CONSTRAINT rhn_actioncd_file_ic_ck
                             CHECK (import_contents in ('Y','N')),
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_actioncd_aid_uq
    ON rhnActionConfigDate (action_id)
    ;

