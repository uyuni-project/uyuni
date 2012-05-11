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

CREATE TABLE suseCredentials
(
    id       NUMBER NOT NULL
                 CONSTRAINT suse_credentials_pk PRIMARY KEY,
    user_id  NUMBER NOT NULL
                 CONSTRAINT suse_credentials_user_fk
                 REFERENCES web_contact (id)
                 ON DELETE CASCADE,
    type_id  NUMBER NOT NULL
                 CONSTRAINT suse_credentials_type_fk
                 REFERENCES suseCredentialsType (id),
    url      VARCHAR2(256),
    username VARCHAR2(64) NOT NULL,
    password VARCHAR2(64) NOT NULL,
    created  DATE DEFAULT (sysdate) NOT NULL,
    modified DATE DEFAULT (sysdate) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_credentials_id_seq;

