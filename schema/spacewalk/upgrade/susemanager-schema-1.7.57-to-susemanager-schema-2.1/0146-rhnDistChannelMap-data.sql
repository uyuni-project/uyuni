-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnDistChannelMap dcm SET org_id = (SELECT c.org_id FROM rhnChannel c where c.id = dcm.channel_id);
