-- oracle equivalent source sha1 5fe31e02cc1526be2aa906d85555805b7f218a6b
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

CREATE TABLE IF NOT EXISTS suseContentProjectHistoryEntry(
    id         NUMERIC NOT NULL
                   CONSTRAINT suse_ct_prj_hist_id_pk PRIMARY KEY,
    project_id NUMERIC NOT NULL
                   CONSTRAINT suse_ct_prj_hist_prjid_fk
                       REFERENCES suseContentProject(id)
                       ON DELETE CASCADE,
    message    TEXT,
    version    NUMERIC NOT NULL,
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    user_id    NUMERIC
                   CONSTRAINT suse_ct_prj_hist_uid_fk
                       REFERENCES web_contact (id)
                       ON DELETE SET NULL
)

;

CREATE SEQUENCE IF NOT EXISTS suse_ct_prj_hist_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_ct_prj_hist_pid_ver_uq
    ON suseContentProjectHistoryEntry(project_id, version);
