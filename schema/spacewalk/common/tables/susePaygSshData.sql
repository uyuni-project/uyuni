--
-- Copyright (c) 2021 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE susePaygSshData
(
    id                   NUMERIC                                 NOT NULL
        CONSTRAINT susePaygSshData_pk PRIMARY KEY,
    description          VARCHAR(255),
    host                 VARCHAR(255)                            NOT NULL,
    port                 NUMERIC,
    username             VARCHAR(32)                             NOT NULL,
    password             VARCHAR(32),
    key                  text,
    key_password         VARCHAR(32),
    bastion_host         VARCHAR(255),
    bastion_port         NUMERIC,
    bastion_username     VARCHAR(32),
    bastion_password     VARCHAR(32),
    bastion_key          text,
    bastion_key_password VARCHAR(32),
    status               CHAR(1) DEFAULT ('P')               NOT NULL
        CONSTRAINT suse_payg_ssh_data_status_ck
            CHECK (status in ('P', 'E', 'S')),
    error_message       text,
    created              TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE susePaygSshData_id_seq;

CREATE UNIQUE INDEX susePaygSshData_host_uq
    ON susePaygSshData (host);