--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

CREATE INDEX IF NOT EXISTS rhn_action_org_earliest_id_unarch_idx
    ON rhnAction(org_id, earliest_action DESC, id)
    WHERE archived = 0;

CREATE INDEX IF NOT EXISTS rhn_action_org_earliest_id_arch_idx
    ON rhnAction(org_id, earliest_action DESC, id)
    WHERE archived = 1;

CREATE INDEX IF NOT EXISTS rhn_ser_act_aid_status_sid_idx
    ON rhnServerAction(action_id, status, server_id);
