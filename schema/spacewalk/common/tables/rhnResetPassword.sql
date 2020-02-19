--
-- Copyright (c) 2015 Red Hat, Inc.
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


CREATE TABLE rhnResetPassword
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_rstpwd_id_pk primary key,
    user_id       NUMERIC NOT NULL
                      CONSTRAINT rhn_rstpwd_uid_fk REFERENCES web_contact (id)
                      ON DELETE CASCADE,
    token         VARCHAR(64) NOT NULL
                      CONSTRAINT rhn_rstpwd_token_uq UNIQUE,
    is_valid      char(1) DEFAULT ('Y') NOT NULL
                      CONSTRAINT rhn_rstpwd_is_valid_ck CHECK (is_valid IN ('Y', 'N')),
    created       TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE rhn_reset_password_id_seq START WITH 500;
