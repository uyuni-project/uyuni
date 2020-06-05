-- Copyright (c) 2020 SUSE LLC
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

CREATE TABLE IF NOT EXISTS suseClusters (
    id          NUMERIC NOT NULL
                    CONSTRAINT suse_clusters_id_pk PRIMARY KEY,
    org_id      NUMERIC NOT NULL
                    CONSTRAINT rhn_server_oid_fk
                    REFERENCES web_customer (id)
                    ON DELETE CASCADE,
    label       VARCHAR(256) NOT NULL,
    name        VARCHAR(256) NOT NULL,
    description VARCHAR(4096) DEFAULT '',
    provider    VARCHAR(50) NOT NULL,
    management_node_id  NUMERIC
                        CONSTRAINT suse_clusters_mgmt_node_fk
                        REFERENCES rhnServer (id)
                        ON DELETE SET NULL,
    group_id    NUMERIC NOT NULL
                    CONSTRAINT suse_clusters_group_fk
                    REFERENCES rhnServerGroup (id),
    created     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS suse_cluster_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_cluster_name_uq
    ON suseClusters (name);

CREATE UNIQUE INDEX IF NOT EXISTS suse_cluster_label_uq
    ON suseClusters (label);

CREATE UNIQUE INDEX IF NOT EXISTS suse_cluster_group_uq
    ON suseClusters (group_id);
