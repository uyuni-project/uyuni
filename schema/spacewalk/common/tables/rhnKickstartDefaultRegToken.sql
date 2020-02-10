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


CREATE TABLE rhnKickstartDefaultRegToken
(
    kickstart_id  NUMERIC NOT NULL
                      CONSTRAINT rhn_ksdrt_ksid_fk
                          REFERENCES rhnKSData (id)
                          ON DELETE CASCADE,
    regtoken_id   NUMERIC NOT NULL
                      CONSTRAINT rhn_ksdrt_rtid_fk
                          REFERENCES rhnRegToken (id)
                          ON DELETE CASCADE,
    created       TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ksdrt_ksid_rtid_idx
    ON rhnKickstartDefaultRegToken (kickstart_id, regtoken_id)
    ;

CREATE INDEX rhn_ksdrt_rtid_idx
    ON rhnKickstartDefaultRegToken (regtoken_id)
    ;

