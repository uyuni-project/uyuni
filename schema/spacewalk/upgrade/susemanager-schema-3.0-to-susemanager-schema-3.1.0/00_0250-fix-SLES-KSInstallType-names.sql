-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnKSInstallType set name = 'SUSE Linux Enterprise 10' where label = 'sles10generic';
update rhnKSInstallType set name = 'SUSE Linux Enterprise 11' where label = 'sles11generic';
update rhnKSInstallType set name = 'SUSE Linux Enterprise 12' where label = 'sles12generic';
