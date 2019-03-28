-- oracle equivalent source sha1 4de4b59b8633c20373936815312476db7475d58d
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

CREATE TABLE IF NOT EXISTS suseContentProjectSource(
    id         NUMERIC NOT NULL
                   CONSTRAINT suse_ct_prj_src_id_pk PRIMARY KEY,
    type       VARCHAR(16) NOT NULL,
    state      VARCHAR(16) NOT NULL,
    position   NUMERIC,
    project_id NUMERIC NOT NULL
                   CONSTRAINT suse_ct_prj_src_prjid_fk
                       REFERENCES suseContentProject(id)
                       ON DELETE CASCADE,
    channel_id NUMERIC
                   CONSTRAINT suse_ct_prj_src_chanid_fk
                       REFERENCES rhnChannel(id)
                       ON DELETE CASCADE
)

;

CREATE SEQUENCE IF NOT EXISTS suse_ct_prj_src_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_ct_prj_src_pid_cid_uq
    ON suseContentProjectSource(project_id, channel_id);

CREATE INDEX IF NOT EXISTS suse_ct_prj_src_type
    ON suseContentProjectSource(type);
