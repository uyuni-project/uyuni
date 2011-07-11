--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

CREATE TABLE suseImages
(
    id                 NUMBER
                           CONSTRAINT suse_images_id_pk PRIMARY KEY
                           USING INDEX TABLESPACE [[4m_tbs]],
    org_id             NUMBER
                           CONSTRAINT suse_images_org_fk
                               REFERENCES web_customer (id)
			    ON DELETE CASCADE,
    name               VARCHAR2(254) NOT NULL,
    version            VARCHAR2(64) NOT NULL,
    imgtype            VARCHAR2(10) NOT NULL,
    path               VARCHAR2(1000) NOT NULL,
    checksum           VARCHAR2(128) NOT NULL,
    created            DATE
                           DEFAULT (sysdate) NOT NULL,
    modified           DATE
                           DEFAULT (sysdate) NOT NULL,
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_images_id_seq;

