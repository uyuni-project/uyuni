--
-- Copyright (c) 2014--2018 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

delete from susesccrepository;

ALTER TABLE suseSCCRepository
  ALTER COLUMN scc_id SET NOT NULL,
  ALTER COLUMN name SET NOT NULL,
  ALTER COLUMN description SET NOT NULL,
  ALTER COLUMN url SET NOT NULL;
  
alter table suseSCCRepository drop column if exists credentials_id;
alter table suseSCCRepository add column if not exists
    signed CHAR(1) DEFAULT ('N') NOT NULL;

alter table suseSCCRepository drop CONSTRAINT if exists suse_sccrepo_sig_ck;
alter table suseSCCRepository add CONSTRAINT suse_sccrepo_sig_ck CHECK (signed in ('Y', 'N'));

DROP INDEX if exists suse_sccrepo_sccid_idx;
CREATE UNIQUE INDEX if not exists suse_sccrepo_sccid_uq
    ON suseSCCRepository (scc_id);

CREATE INDEX if not exists suse_sccrepo_url_idx
    ON suseSCCRepository (url);
