-- oracle equivalent source none
--
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

CREATE UNIQUE INDEX suse_rec_action_name_minion_uq
    ON suseRecurringAction(name, minion_id)
    WHERE group_id IS NULL AND org_id IS NULL;

CREATE UNIQUE INDEX suse_rec_action_name_grp_uq
    ON suseRecurringAction(name, group_id)
    WHERE minion_id IS NULL AND org_id IS NULL;

CREATE UNIQUE INDEX suse_rec_action_name_org_uq
    ON suseRecurringAction(name, org_id)
    WHERE minion_id IS NULL AND group_id IS NULL;

