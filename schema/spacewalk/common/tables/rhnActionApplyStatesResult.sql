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

CREATE TABLE rhnActionApplyStatesResult
(
    server_id              NUMERIC NOT NULL
                               CONSTRAINT rhn_apply_states_result_sid_fk
                                   REFERENCES rhnServer (id)
                                   ON DELETE CASCADE,
    action_apply_states_id NUMERIC NOT NULL
                               CONSTRAINT rhn_apply_states_result_aid_fk
                                   REFERENCES rhnActionApplyStates (id)
                                   ON DELETE CASCADE,
    output                 BYTEA,
    return_code            NUMERIC NOT NULL
)

;

CREATE UNIQUE INDEX rhn_apply_states_result_sa_uq
    ON rhnActionApplyStatesResult (server_id, action_apply_states_id);

CREATE INDEX rhn_apply_states_result_ad_idx
    ON rhnActionApplyStatesResult (action_apply_states_id)
    ;

