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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnKSData
(
    id              NUMERIC NOT NULL
                        CONSTRAINT rhn_ks_id_pk PRIMARY KEY
                        ,
    ks_type         VARCHAR(8) NOT NULL,
    org_id          NUMERIC NOT NULL
                        CONSTRAINT rhn_ks_oid_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE,
    is_org_default  CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_default_ck
                            CHECK (is_org_default in ('Y','N')),
    label           VARCHAR(64) NOT NULL,
    comments        VARCHAR(4000),
    active          CHAR(1)
                        DEFAULT ('Y') NOT NULL
                        CONSTRAINT rhn_ks_active_ck
                            CHECK (active in ('Y','N')),
    postLog         CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_post_log_ck
                            CHECK (postLog in ('Y','N')),
    preLog          CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_pre_log_ck
                            CHECK (preLog in ('Y','N')),
    kscfg           CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_cfg_save_ck
                            CHECK (kscfg in ('Y','N')),
    cobbler_id      VARCHAR(64),
    pre             BYTEA,
    post            BYTEA,
    nochroot_post   BYTEA,
    partition_data   BYTEA,
    static_device   VARCHAR(32),
    kernel_params   VARCHAR(2048),
    verboseup2date  CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_verbose_up2date_ck
                            CHECK (verboseup2date in ('Y','N')),
    nonchrootpost   CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_nonchroot_post_ck
                            CHECK (nonchrootpost in ('Y','N')),
    no_base         CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_nobase_ck
                            CHECK (no_base in ( 'Y' , 'N' )),
    ignore_missing  CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ks_ignore_missing_ck
                            CHECK (ignore_missing in ( 'Y' , 'N' )),
    created         TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    modified        TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    update_type     VARCHAR(7) DEFAULT ('none') NOT NULL
                        CONSTRAINT rhn_ks_update_type
			    CHECK (update_type in ('all', 'red_hat', 'none')),
    CONSTRAINT rhn_ks_type_ck
        CHECK (ks_type in ('wizard','raw'))
)

;

CREATE INDEX rhn_ks_oid_label_id_idx
    ON rhnKSData (org_id, label, id)
    ;

CREATE SEQUENCE rhn_ks_id_seq;

ALTER TABLE rhnKSData
    ADD CONSTRAINT rhn_ks_oid_label_uq UNIQUE (org_id, label);

