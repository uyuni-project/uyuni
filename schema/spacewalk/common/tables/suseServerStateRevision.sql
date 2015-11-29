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

CREATE TABLE suseServerStateRevision
(
    server_id         NUMBER NOT NULL
                          CONSTRAINT suse_server_state_rev_sid_fk
                              REFERENCES rhnServer (id)
                              ON DELETE CASCADE,
    state_revision_id NUMBER NOT NULL
                          CONSTRAINT suse_server_state_rev_id_fk
                              REFERENCES suseStateRevision (id)
                              ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
;

ALTER TABLE suseServerStateRevision
    ADD CONSTRAINT suse_server_state_rev_id_pk PRIMARY KEY (state_revision_id);

ALTER TABLE suseServerStateRevision
    ADD CONSTRAINT suse_server_state_rev_id_sid_uq UNIQUE (server_id, state_revision_id);
