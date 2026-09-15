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


CREATE TABLE rhnKickstartableTree
(
    id              NUMERIC NOT NULL,
    org_id          NUMERIC
                        CONSTRAINT rhn_kstree_oid_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE,
    label           VARCHAR(64) NOT NULL,
    base_path       VARCHAR(256) NOT NULL,
    channel_id      NUMERIC NOT NULL
                        CONSTRAINT rhn_kstree_cid_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    cobbler_id      VARCHAR(64),
    cobbler_xen_id  VARCHAR(64),
    boot_image      VARCHAR(128)
                        DEFAULT ('spacewalk-koan'),
    kstree_type     NUMERIC NOT NULL
                        CONSTRAINT rhn_kstree_kstreetype_fk
                            REFERENCES rhnKSTreeType (id),
    install_type    NUMERIC NOT NULL
                        CONSTRAINT rhn_kstree_it_fk
                            REFERENCES rhnKSInstallType (id),
    kernel_options       VARCHAR(2048),
    kernel_options_post  VARCHAR(2048),
    last_modified   TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    created         TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    modified        TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_kstree_oid_label_uq
    ON rhnKickstartableTree (org_id, label)
    ;

CREATE SEQUENCE rhn_kstree_id_seq;

ALTER TABLE rhnKickstartableTree
    ADD CONSTRAINT rhn_kstree_id_pk PRIMARY KEY (id)
    ;

