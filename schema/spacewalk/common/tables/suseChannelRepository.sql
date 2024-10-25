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

CREATE TABLE suseChannelRepository
(
    sccchannel_id  BIGINT NOT NULL
                     CONSTRAINT suse_chanrepo_cid_fk
                         REFERENCES suseChannelAttributes (id)
                         ON DELETE CASCADE,
    sccrepo_id     NUMERIC NOT NULL
                     CONSTRAINT suse_chanrepo_rid_fk
                         REFERENCES suseSCCRepository (id)
			ON DELETE CASCADE
);

CREATE UNIQUE INDEX suse_chanrepo_cid_rid_uq
    ON suseChannelRepository (sccchannel_id, sccrepo_id);

CREATE INDEX suse_chanrepo_rid_idx
    ON suseChannelRepository(sccrepo_id);
