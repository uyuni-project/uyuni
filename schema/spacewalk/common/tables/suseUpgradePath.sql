--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--


create table
suseUpgradePath
(
    from_pdid     number not null
                  CONSTRAINT suse_upgpath_fromid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    to_pdid       number not null
                  CONSTRAINT suse_upgpath_toid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX suseupgpath_fromid_idx
ON suseUpgradePath (from_pdid)
TABLESPACE [[64k_tbs]];

