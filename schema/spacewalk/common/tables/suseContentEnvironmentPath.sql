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

CREATE TABLE suseContentEnvironmentPath(
    env_id NUMBER
        CONSTRAINT suse_ct_env_path_eid_fk
            REFERENCES suseContentEnvironment(id),
    next_env_id NUMBER
        CONSTRAINT suse_ct_env_path_nxt_eid_fk
            REFERENCES suseContentEnvironment(id)
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_ct_env_pid_uq
    ON suseContentEnvironmentPath(env_id);

CREATE UNIQUE INDEX suse_ct_env_nxt_pid_uq
    ON suseContentEnvironmentPath(next_env_id);

