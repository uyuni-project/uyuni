-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id)
values (lookup_sg_type('bootstrap_entitled'), lookup_feature_type('ftr_kickstart'));

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id)
values (lookup_sg_type('bootstrap_entitled'), lookup_feature_type('ftr_system_grouping'));

