--
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


CREATE TABLE rhnChannel
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_channel_id_pk PRIMARY KEY
                            ,
    parent_channel      NUMERIC
                            CONSTRAINT rhn_channel_parent_ch_fk
                                REFERENCES rhnChannel (id),
    org_id              NUMERIC
                            CONSTRAINT rhn_channel_org_fk
                                REFERENCES web_customer (id),
    channel_arch_id     NUMERIC NOT NULL
                            CONSTRAINT rhn_channel_caid_fk
                                REFERENCES rhnChannelArch (id),
    label               VARCHAR(128) NOT NULL,
    basedir             VARCHAR(256) NOT NULL,
    name                VARCHAR(256) NOT NULL,
    summary             VARCHAR(500) NOT NULL,
    description         VARCHAR(4000),
    product_name_id     NUMERIC
                            CONSTRAINT rhn_channel_product_name_ch_fk
                                REFERENCES rhnProductName (id),
    gpg_check           CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_channel_gc_ck
                                CHECK (gpg_check in ('Y', 'N')),
    gpg_key_url         VARCHAR(256),
    gpg_key_id          VARCHAR(14),
    gpg_key_fp          VARCHAR(50),
    end_of_life         TIMESTAMPTZ,
    checksum_type_id    NUMERIC CONSTRAINT rhn_channel_checksum_fk
                                REFERENCES rhnChecksumType(id),
    receiving_updates   CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_channel_ru_ck
                                CHECK (receiving_updates in ('Y', 'N')),
    last_modified       TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    last_synced         TIMESTAMPTZ,
    channel_product_id  NUMERIC
                            CONSTRAINT rhn_channel_cpid_fk
                                REFERENCES rhnChannelProduct (id),
    channel_access      VARCHAR(10)
                            DEFAULT ('private'),
    maint_name          VARCHAR(128),
    maint_email         VARCHAR(128),
    maint_phone         VARCHAR(128),
    support_policy      VARCHAR(256),
    update_tag          VARCHAR(128),
    installer_updates   CHAR(1)
                            DEFAULT ('N') NOT NULL
                            CONSTRAINT rhn_channel_instup_ck
                                CHECK (installer_updates in ('Y', 'N')),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_channel_label_uq
    ON rhnChannel (label)
    ;

CREATE UNIQUE INDEX rhn_channel_name_uq
    ON rhnChannel (name)
    ;

CREATE INDEX rhn_channel_org_idx
    ON rhnChannel (org_id, id)
    
    ;

CREATE INDEX rhn_channel_parent_id_idx
    ON rhnChannel (parent_channel, id)
    
    ;

CREATE INDEX rhn_channel_access_idx
    ON rhnChannel (channel_access)
    
    ;

CREATE SEQUENCE rhn_channel_id_seq START WITH 101;

