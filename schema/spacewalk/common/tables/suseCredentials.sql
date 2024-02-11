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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseCredentials
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT suse_credentials_pk PRIMARY KEY,
    user_id             NUMERIC NULL
                            CONSTRAINT suse_credentials_user_fk
                            REFERENCES web_contact (id)
                            ON DELETE CASCADE,
    type                VARCHAR(128) DEFAULT ('scc') NOT NULL
                             CONSTRAINT rhn_type_ck
                             CHECK (type IN ('scc', 'vhm', 'registrycreds', 'cloudrmt', 'reportcreds', 'rhui')),
    url                 VARCHAR(256),
    username            VARCHAR(64),
    password            VARCHAR(4096),
    created             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    extra_auth          bytea,
    payg_ssh_data_id    NUMERIC
                            CONSTRAINT suse_credentials_payg_ssh_data_id_fk REFERENCES susePaygSshData (id)
);

ALTER TABLE susecredentials
    ADD CONSTRAINT suse_credentials_payg_ssh_data_id_uq UNIQUE (payg_ssh_data_id);

ALTER TABLE susecredentials
    ADD CONSTRAINT cred_type_check CHECK (
        CASE type
            WHEN 'scc' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'cloudrmt' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
                    and url is not null and url <> ''
            WHEN 'vhm' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'registrycreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'reportcreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
        END
    );

CREATE SEQUENCE suse_credentials_id_seq;
