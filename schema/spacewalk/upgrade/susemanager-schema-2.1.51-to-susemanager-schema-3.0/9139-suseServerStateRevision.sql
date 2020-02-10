-- oracle equivalent source sha1 9765e462e1ae52cb940e2cfd9f6375541cb740d6
--
-- Copyright (c) 2015 SUSE LLC
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
-- CONSTRAINT names are different in the oracle version
-- pg names will be aligned again with 3.0.13/010-shorten-constraints-names

CREATE TABLE suseServerStateRevision
(
    server_id         NUMERIC NOT NULL
                          CONSTRAINT suse_server_state_rev_sid_fk
                              REFERENCES rhnServer (id)
                              ON DELETE CASCADE,
    state_revision_id NUMERIC NOT NULL
                          CONSTRAINT suse_server_state_rev_id_fk
                              REFERENCES suseStateRevision (id)
                              ON DELETE CASCADE
)

;

ALTER TABLE suseServerStateRevision
    ADD CONSTRAINT suse_server_state_rev_id_sid_uq UNIQUE (server_id, state_revision_id);
