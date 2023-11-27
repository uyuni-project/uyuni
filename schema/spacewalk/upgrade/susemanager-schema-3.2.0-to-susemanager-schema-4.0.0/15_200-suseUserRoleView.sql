--
-- Copyright (c) 2018 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE OR REPLACE VIEW
suseChannelUserRoleView
AS
  SELECT
    combination.channel_id,
    combination.user_id,
    combination.role,
    CASE
      -- if channel is in an org trusted by the user's org, and if the channel itself is shared between the two, then user can subscribe it
      WHEN combination.role = 'subscribe' AND combination.user_id IN (
        SELECT u.id
        FROM web_contact u
          JOIN rhnSharedChannelView s ON s.org_trust_id = u.org_id
        WHERE channel_id = combination.channel_id
      )
        THEN NULL
      -- otherwise, if channel is not in user's org, then user can't manage it
      WHEN combination.role = 'manage' AND combination.channel_org_id IS NULL OR combination.channel_org_id <> combination.user_org_id
        THEN 'channel_not_owned'
      -- otherwise, if channel is in a family without permissions for the user's org, then user can't subscribe it
      WHEN combination.role = 'subscribe' AND combination.user_org_id NOT IN (
        SELECT cfp.org_id
          FROM rhnChannelFamilyMembers cfm
            JOIN rhnOrgChannelFamilyPermissions cfp ON cfp.channel_family_id = cfm.channel_family_id
            WHERE cfm.channel_id = combination.channel_id
      )
        THEN 'channel_not_available'
      -- otherwise, if user is a channel admin or an org admin, he can both manage and subscribe it
      WHEN combination.user_id IN (
        SELECT u.id
          FROM web_contact u
            JOIN rhnUserGroupMembers m ON m.user_id = u.id
            JOIN rhnUserGroup g ON g.id = m.user_group_id
            JOIN rhnUserGroupType t ON t.id = g.group_type
          WHERE t.label = 'channel_admin' OR t.label = 'org_admin'
      )
        THEN NULL
      -- otherwise, if channel does not have the "not_globally_subscribable" bit set, user can subscribe it
      WHEN combination.role = 'subscribe' AND combination.channel_id NOT IN (
        SELECT ocs.channel_id
        FROM rhnOrgChannelSettings ocs
          JOIN rhnOrgChannelSettingsType ocst ON ocst.id = ocs.setting_id
        WHERE ocst.label = 'not_globally_subscribable' AND ocs.org_id = combination.user_org_id
      )
        THEN NULL
      -- otherwise, user might have an explicit permission on this channel
      WHEN combination.role IN (
        SELECT cpr.label
          FROM rhnChannelPermission cp
            JOIN rhnChannelPermissionRole cpr ON cpr.id = cp.role_id
          WHERE cp.channel_id = combination.channel_id AND cp.user_id = combination.user_id
      )
        THEN NULL
        -- otherwise, user can't either manage nor subscribe the channel
        ELSE 'direct_permission'
    END AS deny_reason
    FROM
      (SELECT
          c.id AS channel_id,
          c.org_id AS channel_org_id,
          u.id AS user_id,
          u.org_id AS user_org_id,
          r.label AS role
          FROM rhnChannel c, web_contact u, rhnChannelPermissionRole r
      ) combination
    -- ORDER BY channel_id, user_id, role, result
;
