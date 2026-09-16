-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnDistChannelMap drop constraint rhn_dcm_release_caid_oid_uq;

create unique index rhn_dcm_rel_caid_oid_uq_idx
    on rhnDistChannelMap (release, channel_arch_id, org_id)
 where org_id is not null;

create unique index rhn_dcm_rel_caid_uq_idx
    on rhnDistChannelMap (release, channel_arch_id)
 where org_id is null;

