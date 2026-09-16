-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

delete from rhnServerGroupTypeFeature where server_group_type_id = lookup_sg_type('monitoring_entitled');
