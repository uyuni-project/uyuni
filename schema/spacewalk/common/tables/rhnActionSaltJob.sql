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

CREATE TABLE rhnActionSaltJob
(
    action_id        NUMBER NOT NULL
                         CONSTRAINT rhn_act_saltjob_act_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    jid              VARCHAR2(2048) NOT NULL,
    data             BLOB
)
TABLESPACE [[blob]]
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_act_saltjob_aid_idx
    ON rhnActionSaltJob (action_id)
    TABLESPACE [[4m_tbs]]
    NOLOGGING;

ALTER TABLE rhnActionSaltJob
    ADD CONSTRAINT rhn_saltjob_aid_pk PRIMARY KEY (action_id);

CREATE INDEX rhn_act_saltjob_jid_idx
    ON rhnActionSaltJob (jid)
    TABLESPACE [[4m_tbs]]
    NOLOGGING;

