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

CREATE TABLE suseVirtualHostManagerNodeInfo
(
    id            NUMBER NOT NULL
                    CONSTRAINT suse_vhmnode_id_pk PRIMARY KEY,
    identifier    VARCHAR2(1024) NOT NULL,
    name          VARCHAR2(128),
    node_arch_id  NUMBER
                    CONSTRAINT rhn_vhmnodeinf_said_fk
                    REFERENCES rhnServerArch (id),
    cpu_sockets   NUMBER,
    cpu_cores     NUMBER,
    ram           NUMBER,
    os            VARCHAR2(64) NOT NULL,
    os_version    VARCHAR2(64) NOT NULL,
    created       timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL,
    modified      timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_vhm_nodeinfo_id_seq;

