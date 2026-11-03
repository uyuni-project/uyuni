-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnTimezone
   set olson_name = 'Australia/Perth'
 where olson_name = 'Asia/Hong_Kong' and display_name = 'Australia (Western)';
