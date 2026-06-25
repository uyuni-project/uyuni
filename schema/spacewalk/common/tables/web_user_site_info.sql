--
-- Copyright (c) 2026 SUSE
-- Copyright (c) 2008--2012 Red Hat, Inc.
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


CREATE TABLE web_user_site_info
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT wusi_id_pk PRIMARY KEY,
    web_user_id         NUMERIC
                            CONSTRAINT wusi_wuid_fk
                                REFERENCES web_contact (id)
                                ON DELETE CASCADE,
    email               VARCHAR(128),
    address1            VARCHAR(128) NOT NULL,
    address2            VARCHAR(128),
    city                VARCHAR(128) NOT NULL,
    state               VARCHAR(64),
    zip                 VARCHAR(64),
    country             CHAR(2) NOT NULL,
    phone               VARCHAR(32),
    fax                 VARCHAR(32),
    is_po_box           BOOLEAN NOT NULL DEFAULT FALSE,
    notes               VARCHAR(2000),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp),
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp)
)

;

CREATE INDEX web_user_site_info_wuid
    ON web_user_site_info (web_user_id);

CREATE SEQUENCE web_user_site_info_id_seq;

