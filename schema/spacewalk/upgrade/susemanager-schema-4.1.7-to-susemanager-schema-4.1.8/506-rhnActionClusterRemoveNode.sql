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

CREATE TABLE IF NOT EXISTS rhnActionClusterRemoveNode (
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actionclrmnode_aid_fk
                            REFERENCES rhnAction (id)
                            ON DELETE CASCADE,
    cluster_id          NUMERIC NOT NULL
                            CONSTRAINT rhn_actionclrmnode_cluster_fk
                            REFERENCES suseClusters (id)
                            ON DELETE CASCADE,
    json_params         TEXT,
    created             TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)
;

CREATE UNIQUE INDEX IF NOT EXISTS rhnactionclusterrn_aid_uq ON rhnActionClusterRemoveNode (action_id);

insert into rhnActionType values (517, 'cluster.remove_node', 'Remove node from cluster', 'N', 'N') on conflict do nothing;
