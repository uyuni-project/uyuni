--
-- Copyright (c) 2021 SUSE LLC
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

CREATE TABLE suseCloudRmtHost
(
    id               NUMERIC                                 NOT NULL
        CONSTRAINT suseCloudRmtHost_pk PRIMARY KEY,
    hostname         VARCHAR(255)                            NOT NULL,
    ip_address       VARCHAR(39)                             NOT NULL,
    ssl_cert         text,
    created          TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    payg_ssh_data_id NUMERIC                                 NOT NULL,
    CONSTRAINT payg_ssh_data_id_fk
        FOREIGN KEY (payg_ssh_data_id) REFERENCES susePaygSshData (id)
);

CREATE SEQUENCE susecloudrmthost_id_seq;