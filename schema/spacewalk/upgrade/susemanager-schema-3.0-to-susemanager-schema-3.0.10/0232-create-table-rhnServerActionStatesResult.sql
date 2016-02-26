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

CREATE TABLE rhnServerActionStatesResult
(
    server_id              NUMBER NOT NULL
                               CONSTRAINT rhn_states_result_sid_fk
                                   REFERENCES rhnServer (id),
    action_apply_states_id NUMBER NOT NULL
                               CONSTRAINT rhn_states_result_aasid_fk
                                   REFERENCES rhnActionApplyStates (id)
                                   ON DELETE CASCADE,
    output                 BLOB,
    return_code            NUMBER NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_states_result_saas_uq
    ON rhnServerActionStatesResult (server_id, action_apply_states_id);

CREATE INDEX rhn_states_result_aasid_idx
    ON rhnServerActionStatesResult (action_apply_states_id)
    NOLOGGING;

