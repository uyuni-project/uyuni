--
-- Copyright (c) 2015 SUSE LLC
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

CREATE TABLE suseVirtualHostManager
(
    id          NUMERIC NOT NULL
                    CONSTRAINT suse_vhms_id_pk PRIMARY KEY,
    org_id      NUMERIC NOT NULL
                    CONSTRAINT suse_vhms_oid_fk
                    REFERENCES web_customer (id)
                    ON DELETE CASCADE,
    label       VARCHAR(128) NOT NULL,
    gatherer_module     VARCHAR(50) NOT NULL,
    cred_id     NUMERIC
                    CONSTRAINT suse_vhms_creds_fk
                    REFERENCES suseCredentials (id)
                    ON DELETE SET NULL,
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX suse_vhm_label_uq
ON suseVirtualHostManager (label)
;

CREATE SEQUENCE suse_vhms_id_seq;

