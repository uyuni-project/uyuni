--
-- Copyright (c) 2018 SUSE LLC
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

CREATE TABLE suseContentEnvironment(
    id          NUMBER NOT NULL
                    CONSTRAINT suse_ct_env_id_pk PRIMARY KEY,
    project_id  NUMBER
                    CONSTRAINT suse_ct_env_pid_fk
                        REFERENCES suseContentProject(id)
                        ON DELETE CASCADE,
    label       VARCHAR2(16) NOT NULL,
    name        VARCHAR2(128) NOT NULL,
    description CLOB,
    version     NUMBER,
    next_env_id NUMBER
                    CONSTRAINT suse_ct_env_nid_fk
                        REFERENCES suseContentEnvironment(id),
    created     TIMESTAMP WITH LOCAL TIME ZONE
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMP WITH LOCAL TIME ZONE
                    DEFAULT (current_timestamp) NOT NULL

)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_ct_env_seq;

CREATE UNIQUE INDEX suse_ct_env_pid_lbl_uq
    ON suseContentEnvironment(project_id, label);

CREATE UNIQUE INDEX suse_ct_env_pid_name_uq
    ON suseContentEnvironment(project_id, name);

CREATE UNIQUE INDEX suse_ct_env_nid_uq
    ON suseContentEnvironment(next_env_id);
