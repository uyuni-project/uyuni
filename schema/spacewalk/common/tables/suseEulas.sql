--
-- Copyright (c) 2014 SUSE LINUX Products GmbH, Nuernberg, Germany.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--


CREATE TABLE suseEulas
(
    id          number NOT NULL
                    CONSTRAINT suse_eulas_id_pk PRIMARY KEY,
    text        BLOB,
    checksum    VARCHAR(64) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_eulas_id_seq;

CREATE UNIQUE INDEX suse_eulas_checksum
    on suseEulas (checksum)
    tablespace [[8m_tbs]];

