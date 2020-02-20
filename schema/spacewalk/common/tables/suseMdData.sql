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
    channel_id        NUMERIC
                      CONSTRAINT susemddata_chn_id_fk
                      REFERENCES rhnChannel (id)
                      ON DELETE CASCADE,
    package_id        NUMERIC
                      CONSTRAINT susemddata_pkg_id_fk
                      REFERENCES rhnPackage (id)
                      On DELETE CASCADE,
    keyword_id        NUMERIC
                      CONSTRAINT susemdkeyword_id_fk
                      REFERENCES suseMdKeyword (id),
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX susemddata_chn_pkg_key_uq
ON suseMdData (channel_id, package_id, keyword_id)
;

CREATE INDEX susemddata_chn_pkg_idx
ON suseMdData (channel_id, package_id)
;

