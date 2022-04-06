--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE Account (
    mgm_id                    NUMERIC NOT NULL,
    account_id                NUMERIC NOT NULL,
    username                  VARCHAR(64),
    organization              VARCHAR(128),
    last_name                 VARCHAR(128),
    first_name                VARCHAR(128),
    position                  VARCHAR(128),
    email                     VARCHAR(128),
    creation_time             TIMESTAMPTZ,
    last_login_time           TIMESTAMPTZ,
    status                    VARCHAR(32),
    md5_encryption            BOOLEAN,
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE Account
  ADD CONSTRAINT Account_pk PRIMARY KEY (mgm_id, account_id);
