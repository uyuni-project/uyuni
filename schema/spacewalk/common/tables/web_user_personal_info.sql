--
-- Copyright (c) 2008--2014 Red Hat, Inc.
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


CREATE TABLE web_user_personal_info
(
    web_user_id        NUMERIC NOT NULL
                           CONSTRAINT personal_info_web_user_id_fk
                               REFERENCES web_contact (id)
                               ON DELETE CASCADE,
    prefix             VARCHAR(12)
                           DEFAULT (' ') NOT NULL
                           CONSTRAINT wupi_prefix_fk
                               REFERENCES web_user_prefix (text),
    first_names        VARCHAR(128) NOT NULL,
    last_name          VARCHAR(128) NOT NULL,
    genqual            VARCHAR(12),
    parent_company     VARCHAR(128),
    company            VARCHAR(128),
    title              VARCHAR(128),
    phone              VARCHAR(128),
    fax                VARCHAR(128),
    email              VARCHAR(128),
    email_uc           VARCHAR(128),
    pin                NUMERIC,
    created            TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified           TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    first_names_ol     VARCHAR(128),
    last_name_ol       VARCHAR(128),
    genqual_ol         VARCHAR(12),
    parent_company_ol  VARCHAR(128),
    company_ol         VARCHAR(128),
    title_ol           VARCHAR(128)
)

;

CREATE INDEX wupi_email_uc_idx
    ON web_user_personal_info (email_uc);

CREATE INDEX wupi_user_id_idx
    ON web_user_personal_info (web_user_id);
