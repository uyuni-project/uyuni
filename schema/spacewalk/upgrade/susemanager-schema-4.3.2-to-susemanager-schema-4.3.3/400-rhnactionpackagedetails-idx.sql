--
-- Copyright (c) 2021 SUSE LLC
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

DROP INDEX IF EXISTS rhn_act_eud_aid_idx;

DELETE FROM rhnactionpackagedetails d1
 where d1.id > any (SELECT d2.id
                      FROM rhnactionpackagedetails d2
	             WHERE d1.action_id = d2.action_id);

CREATE UNIQUE INDEX IF NOT EXISTS rhn_act_eud_aid_uq
    ON rhnActionPackageDetails (action_id);
