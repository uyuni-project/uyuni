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

-- First determine which systems belong to wrong entitlement groups
CREATE TEMPORARY VIEW rhnWrongEntitlements AS
  SELECT t1.id, t1.group_id AS wrong_id, t2.id AS right_id
  FROM (SELECT s1.org_id, s1.id, s2.group_id, s2.group_type
    FROM rhnserver AS s1
    JOIN rhnservergroupmembership AS s2
    ON s1.id=s2.server_id
    WHERE s1.org_id != s2.org_id) AS t1
  JOIN (SELECT s1.id, org_id, label
    FROM rhnservergroup AS s1
    JOIN rhnservergrouptype AS s2
    ON group_type=s2.id) AS t2
  ON t1.group_type = t2.label AND t1.org_id = t2.org_id;

-- Update wrong entitlements to the right ones
UPDATE rhnServerGroupMembers
SET server_group_id = rhnWrongEntitlements.right_id
FROM rhnWrongEntitlements
WHERE
  rhnServerGroupMembers.server_id = rhnWrongEntitlements.id
  AND rhnServerGroupMembers.server_group_id = rhnWrongEntitlements.wrong_id;

-- Next we also need to update the member count of each group
-- We set current members to 0 for all the groups to also account for groups
-- that don't have any systems assigned to them.
UPDATE rhnServerGroup
SET current_members = 0;

-- Count the number of systems that belong to each group and
-- set the correct member count
UPDATE rhnServerGroup
SET current_members = t1.count
FROM
  (SELECT server_group_id, count(server_id)
  FROM rhnservergroupmembers
  GROUP BY server_group_id) AS t1
WHERE rhnServerGroup.id = t1.server_group_id;

COMMIT;
