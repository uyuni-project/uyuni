-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnPrivateChannelFamily
   set fve_current_members = rhn_channel.cfam_curr_fve_members(channel_family_id,org_id);

