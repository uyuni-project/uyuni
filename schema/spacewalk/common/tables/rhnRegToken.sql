--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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


CREATE TABLE rhnRegToken
(
    id              NUMERIC NOT NULL
                        CONSTRAINT rhn_reg_token_pk PRIMARY KEY,
    org_id          NUMERIC NOT NULL
                        CONSTRAINT rhn_reg_token_oid_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE,
    user_id         NUMERIC
                        CONSTRAINT rhn_reg_token_uid_fk
                            REFERENCES web_contact (id)
                            ON DELETE SET NULL,
    server_id       NUMERIC
                        CONSTRAINT rhn_reg_token_sid_fk
                            REFERENCES rhnServer (id),
    note            VARCHAR(2048) NOT NULL,
    usage_limit     NUMERIC
                        DEFAULT (0),
    disabled        NUMERIC
                        DEFAULT (0) NOT NULL,
    deploy_configs  CHAR(1)
                        DEFAULT ('Y') NOT NULL
                        CONSTRAINT rhn_reg_token_deployconfs_ck
                            CHECK (deploy_configs in ('Y','N')),
    contact_method_id NUMERIC
                        DEFAULT (0) NOT NULL
                        CONSTRAINT rhn_reg_token_cmid_fk
                            REFERENCES suseServerContactMethod (id)
)

;

CREATE INDEX rhn_reg_token_org_id_idx
    ON rhnRegToken (org_id, id)
    
    ;

CREATE INDEX rhn_reg_token_uid_idx
    ON rhnRegToken (user_id)
    
    ;

CREATE INDEX rhn_reg_token_sid_idx
    ON rhnRegToken (server_id)
    
    ;

CREATE SEQUENCE rhn_reg_token_seq;

