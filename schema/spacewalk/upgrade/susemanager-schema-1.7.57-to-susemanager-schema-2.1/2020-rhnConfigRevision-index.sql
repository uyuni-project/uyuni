-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create index rhn_confrevision_ccid_idx on rhnConfigRevision (config_content_id);
