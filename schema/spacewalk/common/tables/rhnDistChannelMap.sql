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


CREATE TABLE rhnDistChannelMap
(
    id               NUMBER NOT NULL
                        CONSTRAINT rhn_dcm_id_pk PRIMARY KEY,
    os               VARCHAR2(64) NOT NULL,
    release          VARCHAR2(64) NOT NULL,
    channel_arch_id  NUMBER NOT NULL
                         CONSTRAINT rhn_dcm_caid_fk
                             REFERENCES rhnChannelArch (id),
    channel_id       NUMBER NOT NULL
                         CONSTRAINT rhn_dcm_cid_fk
                             REFERENCES rhnChannel (id)
                             ON DELETE CASCADE,
    org_id           NUMBER
                        CONSTRAINT rhn_dcm_oid_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE rhn_dcm_id_seq;

