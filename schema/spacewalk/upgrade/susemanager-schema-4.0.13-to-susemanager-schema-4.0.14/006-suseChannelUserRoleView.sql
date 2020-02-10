-- oracle equivalent source sha1 2f8c2473888007662d6ea18d7008d3f8d7009f20
--
-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
DROP VIEW IF EXISTS suseChannelUserRoleView;

CREATE OR REPLACE VIEW
suseChannelUserRoleView
AS
  SELECT
    combination.channel_id,
    combination.user_id,
    combination.role,
    combination.user_org_id AS org_id,
    CASE
      -- if channel is in an org trusted by the user's org, and if the channel itself is shared between the two, then user can subscribe it
      WHEN combination.role = 'subscribe' AND sharedChannels.is_shared_channel IS NOT NULL
        THEN NULL
      -- otherwise, if channel is not in user's org, then user can't manage it
      WHEN combination.role = 'manage' AND combination.channel_org_id IS NULL OR combination.channel_org_id <> combination.user_org_id
        THEN 'channel_not_owned'
      -- otherwise, if channel is in a family without permissions for the user's org, then user can't subscribe it
      WHEN combination.role = 'subscribe' AND channelPermissions.is_channel_available IS NULL
        THEN 'channel_not_available'
      -- otherwise, if channel is in a family without permissions for the user's org, then user can't subscribe it
      WHEN adminUsers.is_admin_user IS NOT NULL
        THEN NULL
      -- otherwise, if channel does not have the "not_globally_subscribable" bit set, user can subscribe it
      WHEN combination.role = 'subscribe' AND notGloballySubscribableChannels.is_not_globally_subscribable IS NULL
        THEN NULL
      -- otherwise, user might have an explicit permission on this channel
      WHEN explicitPermissions.has_explicit_permission IS NOT NULL
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
     LEFT JOIN 
       (SELECT DISTINCT s.org_trust_id, s.id, 1 AS is_shared_channel
        FROM rhnSharedChannelView s) sharedChannels
     ON (sharedChannels.id = combination.channel_id AND sharedChannels.org_trust_id = combination.user_org_id)
     LEFT JOIN
       (SELECT DISTINCT cfp.org_id, cfm.channel_id, 1 AS is_channel_available
        FROM rhnChannelFamilyMembers cfm
          JOIN rhnOrgChannelFamilyPermissions cfp ON cfp.channel_family_id = cfm.channel_family_id) channelPermissions
     ON (combination.user_org_id = channelPermissions.org_id AND channelPermissions.channel_id = combination.channel_id)
     LEFT JOIN
       (SELECT DISTINCT m.user_id, 1 AS is_admin_user
        FROM rhnUserGroupMembers m
          JOIN rhnUserGroup g ON g.id = m.user_group_id
          JOIN rhnUserGroupType t ON t.id = g.group_type
        WHERE t.label = 'channel_admin' OR t.label = 'org_admin') adminUsers
     ON (adminUsers.user_id = combination.user_id)
     LEFT JOIN
       (SELECT DISTINCT ocs.channel_id, ocs.org_id, 1 AS is_not_globally_subscribable
        FROM rhnOrgChannelSettings ocs
          JOIN rhnOrgChannelSettingsType ocst ON ocst.id = ocs.setting_id
        WHERE ocst.label = 'not_globally_subscribable') notGloballySubscribableChannels
     ON (notGloballySubscribableChannels.channel_id = combination.channel_id AND notGloballySubscribableChannels.org_id = combination.user_org_id)
     LEFT JOIN
       (SELECT cpr.label, cp.channel_id, cp.user_id, 1 AS has_explicit_permission
        FROM rhnChannelPermission cp
          JOIN rhnChannelPermissionRole cpr ON cpr.id = cp.role_id) explicitPermissions
     ON (explicitPermissions.channel_id = combination.channel_id AND explicitPermissions.user_id = combination.user_id AND explicitPermissions.label = combination.role)
    -- ORDER BY channel_id, user_id, role, result
;
