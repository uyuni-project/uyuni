-- oracle equivalent source sha1 f77ff11522423a51ed4462c0c37bf794eae82e45
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
-- CONSTRAINT names are different in the oracle version
-- pg names will be aligned again with 3.0.13/010-shorten-constraints-names

CREATE TABLE rhnActionApplyStatesResult
(
    server_id              NUMERIC NOT NULL
                               CONSTRAINT rhn_apply_states_result_sid_fk
                                   REFERENCES rhnServer (id)
                                   ON DELETE CASCADE,
    action_apply_states_id NUMERIC NOT NULL
                               CONSTRAINT rhn_apply_states_result_aasid_fk
                                   REFERENCES rhnActionApplyStates (id)
                                   ON DELETE CASCADE,
    output                 BYTEA,
    return_code            NUMERIC NOT NULL
)

;

CREATE UNIQUE INDEX rhn_apply_states_result_saas_uq
    ON rhnActionApplyStatesResult (server_id, action_apply_states_id);

CREATE INDEX rhn_apply_states_result_aasid_idx
    ON rhnActionApplyStatesResult (action_apply_states_id)
    ;

