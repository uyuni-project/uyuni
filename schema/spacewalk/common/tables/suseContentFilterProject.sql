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

CREATE TABLE suseContentFilterProject(
    filter_id NUMBER NOT NULL
        CONSTRAINT suse_ct_filter_fid_fk
            REFERENCES suseContentFilter(id)
            ON DELETE CASCADE,

    project_id NUMBER NOT NULL
        CONSTRAINT suse_ct_filter_pid_fk
            REFERENCES suseContentProject(id)
            ON DELETE CASCADE,

    position NUMBER NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_ct_filter_project_uq
    ON suseContentFilterProject(filter_id, project_id);
