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
    id            NUMBER CONSTRAINT suse_vhmnode_id_pk PRIMARY_KEY,
    node_arch_id  NUMBER
                    CONSTRAINT rhn_server_said_fk
                    REFERENCES rhnServerArch (id),
    cpus          NUMBER,
    memory        NUMBER,
    created       timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL,
    modified      timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_vhm_nodeinfo_id_seq;

