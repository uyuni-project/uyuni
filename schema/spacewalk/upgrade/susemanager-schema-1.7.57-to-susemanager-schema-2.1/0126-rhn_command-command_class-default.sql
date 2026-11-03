-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_command
alter command_class set default '/var/lib/nocpulse/libexec/plugin';

