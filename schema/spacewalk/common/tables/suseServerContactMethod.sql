--
-- Copyright (c) 2013 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseServerContactMethod
(
    id        NUMBER NOT NULL
                  CONSTRAINT suse_cont_meth_id_pk PRIMARY KEY
                  USING INDEX TABLESPACE [[64k_tbs]],
    label     VARCHAR2(64) NOT NULL,
    name      VARCHAR2(128) NOT NULL,
    rank      NUMBER NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE INDEX suse_cont_meth_label_id_idx
    ON suseServerContactMethod (label, id)
    TABLESPACE [[64k_tbs]];

ALTER TABLE suseServerContactMethod
    ADD CONSTRAINT suse_cont_meth_label_uq UNIQUE (label);

