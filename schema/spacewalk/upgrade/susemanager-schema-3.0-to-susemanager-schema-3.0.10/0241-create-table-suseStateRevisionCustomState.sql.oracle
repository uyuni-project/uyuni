--
-- Copyright (c) 2016 SUSE LLC
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

CREATE TABLE suseStateRevisionCustomState
(
    state_revision_id NUMBER NOT NULL
                          CONSTRAINT suse_salt_state_rev_id_fk
                              REFERENCES suseStateRevision (id)
                              ON DELETE CASCADE,
    state_id NUMBER NOT NULL
                          CONSTRAINT suse_salt_state_id_fk
                              REFERENCES suseCustomState (id)
                              ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
;

ALTER TABLE suseStateRevisionCustomState
    ADD CONSTRAINT suse_state_rev_id_state_id_uq UNIQUE (state_revision_id, state_id);
