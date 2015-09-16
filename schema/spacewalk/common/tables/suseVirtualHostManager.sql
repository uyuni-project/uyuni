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

CREATE TABLE suseVirtualHostManager
(
    id          NUMBER NOT NULL
                    CONSTRAINT suse_vhms_id_pk PRIMARY KEY,
    org_id      NUMBER NOT NULL
                    CONSTRAINT suse_vhms_oid_fk
                    REFERENCES web_customer (id)
                    ON DELETE CASCADE,
    label       VARCHAR2(128) NOT NULL,
    gatherer_module     VARCHAR2(50) NOT NULL,
    cred_id     NUMBER
                    CONSTRAINT suse_vhms_creds_fk
                    REFERENCES suseCredentials (id)
                    ON DELETE SET NULL,
    created     timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL,
    modified    timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_vhm_label_uq
ON suseVirtualHostManager (label)
TABLESPACE [[64k_tbs]];

CREATE SEQUENCE suse_vhms_id_seq;

