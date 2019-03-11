-- oracle equivalent source sha1 433aede04eb48bf5a4b68577ee3e59eb0173bd3b
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

CREATE TABLE IF NOT EXISTS suseContentFilterProject(
    id         NUMERIC PRIMARY KEY,
    filter_id  NUMERIC NOT NULL
                   CONSTRAINT suse_ct_filter_fid_fk
                       REFERENCES suseContentFilter(id)
                       ON DELETE CASCADE,
    project_id NUMERIC NOT NULL
                   CONSTRAINT suse_ct_filter_pid_fk
                       REFERENCES suseContentProject(id)
                       ON DELETE CASCADE,
    state      VARCHAR(16) NOT NULL,
    position   NUMERIC
);

CREATE SEQUENCE IF NOT EXISTS suse_ct_f_p_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_ct_filter_project_uq
    ON suseContentFilterProject(filter_id, project_id);
