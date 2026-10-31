-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnkickstartscript set position = position * -1 where script_type = 'post' and chroot = 'N';
