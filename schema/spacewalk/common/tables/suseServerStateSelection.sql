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

CREATE TABLE suseAssignedStates
(
    state_revision_id NUMBER NOT NULL
                          CONSTRAINT suse_server_state_rev_id_fk
                              REFERENCES suseStateRevision (id)
                              ON DELETE CASCADE,
    state_id NUMBER NOT NULL
                          CONSTRAINT suse_salt_state_id_fk
                              REFERENCES suseSaltState (id)
                              ON DELETE CASCADE,
)
ENABLE ROW MOVEMENT
;

ALTER TABLE suseAssignedStates
    ADD CONSTRAINT suse_assigned_states_id_sid_uq UNIQUE (state_revision_id, state_id);



CREATE TABLE suseSaltState
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_salt_state_id_pk PRIMARY KEY,
    org_id           NUMBER NOT NULL
                         CONSTRAINT suse_salt_state_org_id_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE,
    state_name       VARCHAR2(256) NOT NULL

)
ENABLE ROW MOVEMENT
;
CREATE UNIQUE INDEX suse_salt_state_name_org_uq
ON suseSaltState (org_id, state_name)
TABLESPACE [[8m_tbs]];

CREATE SEQUENCE suse_salt_state_id_seq;