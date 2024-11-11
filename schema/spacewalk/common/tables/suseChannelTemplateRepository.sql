--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseChannelTemplateRepository
(
    template_id  BIGINT NOT NULL
                     CONSTRAINT suse_chantmplrepo_tid_fk
                         REFERENCES suseChannelTemplate (id)
                         ON DELETE CASCADE,
    repo_id      NUMERIC NOT NULL
                     CONSTRAINT suse_chantmplrepo_rid_fk
                         REFERENCES suseSCCRepository (id)
                         ON DELETE CASCADE
);

CREATE UNIQUE INDEX suse_chantmplrepo_tid_rid_uq
    ON suseChannelTemplateRepository (template_id, repo_id);

CREATE INDEX suse_chantmplrepo_rid_idx
    ON suseChannelTemplateRepository(repo_id);
