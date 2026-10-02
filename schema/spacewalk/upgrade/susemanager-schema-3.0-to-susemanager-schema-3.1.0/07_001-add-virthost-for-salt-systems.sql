-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
            lookup_sg_type('virtualization_host'));
