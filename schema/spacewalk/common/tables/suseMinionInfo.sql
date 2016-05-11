--
-- Copyright (c) 2016 SUSE LLC
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

CREATE TABLE suseMinionInfo
(
    server_id          NUMBER NOT NULL
                           CONSTRAINT suse_minion_info_sid_fk
                               REFERENCES rhnServer (id)
                               ON DELETE CASCADE,
    minion_id          VARCHAR2(256) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE INDEX suse_minion_info_sid_idx
    ON suseMinionInfo (server_id)
    TABLESPACE [[2m_tbs]];

ALTER TABLE suseMinionInfo
    ADD CONSTRAINT rhn_minion_info_sid_uq UNIQUE (server_id);

ALTER TABLE suseMinionInfo
    ADD CONSTRAINT rhn_minion_info_miid_uq UNIQUE (minion_id);
