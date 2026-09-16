-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- functional index for rhnISSMaster

create unique index rhn_issm_only_one_default on rhnISSMaster
    (is_current_master) where is_current_master = 'Y';
