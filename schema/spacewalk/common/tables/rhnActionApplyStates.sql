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

CREATE TABLE rhnActionApplyStates
(
    id               NUMBER NOT NULL
                         CONSTRAINT rhn_act_apply_states_id_pk PRIMARY KEY,
    action_id        NUMBER NOT NULL
                         CONSTRAINT rhn_act_apply_states_act_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    states           VARCHAR2(1024),
    test             CHAR(1)
                         DEFAULT ('N') NOT NULL
                         CONSTRAINT rhn_act_apply_states_test_ck
                             CHECK (test in ('Y','N')),
    created          timestamp with local time zone
                         DEFAULT (current_timestamp) NOT NULL,
    modified         timestamp with local time zone
                         DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_act_apply_states_aid_idx
    ON rhnActionApplyStates (action_id)
    NOLOGGING;

CREATE SEQUENCE rhn_act_apply_states_id_seq;

