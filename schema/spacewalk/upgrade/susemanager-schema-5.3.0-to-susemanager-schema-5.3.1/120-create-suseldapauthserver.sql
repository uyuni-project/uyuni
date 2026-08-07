--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Idempotent: safe if re-applied.
CREATE TABLE IF NOT EXISTS suseLdapAuthServer
(
    id                      NUMERIC NOT NULL
                                CONSTRAINT suse_ldap_auth_srv_id_pk PRIMARY KEY,
    label                   VARCHAR(128) NOT NULL,
    enabled                 BOOLEAN NOT NULL DEFAULT TRUE,
    priority                NUMERIC NOT NULL DEFAULT (0),
    server_type             VARCHAR(32) NOT NULL
                                CONSTRAINT suse_ldap_auth_srv_type_ck
                                    CHECK (server_type in ('ACTIVE_DIRECTORY','FREE_IPA','OPEN_LDAP')),
    host                    VARCHAR(256) NOT NULL,
    port                    NUMERIC NOT NULL,
    transport               VARCHAR(16) NOT NULL
                                CONSTRAINT suse_ldap_auth_srv_transp_ck
                                    CHECK (transport in ('PLAIN','LDAPS','STARTTLS')),
    connect_timeout         NUMERIC,
    response_timeout        NUMERIC,
    bind_dn                 VARCHAR(1024),
    credentials_id          NUMERIC
                                CONSTRAINT suse_ldap_auth_srv_cred_fk
                                    REFERENCES suseCredentials (id)
                                    ON DELETE SET NULL,
    user_base_dn            VARCHAR(1024) NOT NULL,
    user_filter             VARCHAR(1024),
    login_attribute         VARCHAR(128),
    first_name_attribute    VARCHAR(128),
    last_name_attribute     VARCHAR(128),
    email_attribute         VARCHAR(128),
    group_base_dn           VARCHAR(1024),
    group_filter            VARCHAR(1024),
    group_name_attribute    VARCHAR(128),
    use_memberof            BOOLEAN NOT NULL DEFAULT FALSE,
    provisioning_mode       VARCHAR(16) NOT NULL DEFAULT ('JIT')
                                CONSTRAINT suse_ldap_auth_srv_prov_ck
                                    CHECK (provisioning_mode in ('JIT','EXISTING_ONLY')),
    default_org_id          NUMERIC
                                CONSTRAINT suse_ldap_auth_srv_org_fk
                                    REFERENCES web_customer (id)
                                    ON DELETE SET NULL,
    auto_join_regular_user  BOOLEAN NOT NULL DEFAULT TRUE,
    root_ca                 TEXT,
    created                 TIMESTAMPTZ
                                DEFAULT (current_timestamp) NOT NULL,
    modified                TIMESTAMPTZ
                                DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_ldap_auth_srv_label_uq
    ON suseLdapAuthServer (label);

CREATE SEQUENCE IF NOT EXISTS suse_ldap_auth_srv_id_seq;
