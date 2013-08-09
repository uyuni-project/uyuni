--
-- Copyright (c) 2013 Red Hat, Inc.
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


CREATE TABLE log
(
    id NUMBER NOT NULL
        CONSTRAINT log_id_pk PRIMARY KEY,
    stamp TIMESTAMP WITH LOCAL TIME ZONE,
    user_id NUMBER
        CONSTRAINT log_user_id_fk
            REFERENCES web_contact_all(id)
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE log_seq;
