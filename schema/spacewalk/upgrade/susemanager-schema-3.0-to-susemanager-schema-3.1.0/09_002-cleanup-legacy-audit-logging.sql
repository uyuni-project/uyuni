-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

SELECT .recreate_trigger('rhnserver');
SELECT .recreate_trigger('rhnservergroup');
SELECT .recreate_trigger('web_contact');

TRUNCATE log CASCADE;
