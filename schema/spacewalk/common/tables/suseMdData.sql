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
suseMdData
(
    channel_id        number
                      CONSTRAINT susemddata_chn_id_fk
                      REFERENCES rhnChannel (id)
                      ON DELETE CASCADE,
    package_id        number
                      CONSTRAINT susemddata_pkg_id_fk
                      REFERENCES rhnPackage (id)
                      On DELETE CASCADE,
    keyword_id        number
                      CONSTRAINT susemdkeyword_id_fk
                      REFERENCES suseMdKeyword (id),
    created           date default(sysdate) not null,
    modified          date default(sysdate) not null
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX susemddata_chn_pkg_key_uq
ON suseMdData (channel_id, package_id, keyword_id)
TABLESPACE [[64k_tbs]];

CREATE INDEX susemddata_chn_pkg_idx
ON suseMdData (channel_id, package_id)
TABLESPACE [[64k_tbs]];

