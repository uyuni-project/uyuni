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

CREATE TABLE suseContentProjectHistory(
    id NUMBER NOT NULL
        CONSTRAINT suse_ct_prj_hist_id_pk PRIMARY KEY,

    project_id NUMBER NOT NULL
        CONSTRAINT suse_ct_prj_hist_prjid_fk
            REFERENCES suseContentProject(id)
            ON DELETE CASCADE,

    text VARCHAR2(2048),

    version NUMBER,

    created TIMESTAMP WITH LOCAL TIME ZONE
        DEFAULT (current_timestamp) NOT NULL,

    user_id NUMBER
        CONSTRAINT suse_ct_prj_hist_uid_fk
            REFERENCES web_contact (id)
            ON DELETE SET NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_ct_prj_hist_seq;

CREATE UNIQUE INDEX suse_ct_prj_hist_pid_ver_uq
    ON suseContentProjectHistory(project_id, version);
