-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table web_contact_log drop column old_password;

alter table web_contact_log alter password type varchar(110);

select .recreate_trigger('web_contact');
