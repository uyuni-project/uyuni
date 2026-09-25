-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnKSInstallType (id, label, name)
        values (sequence_nextval('rhn_ksinstalltype_id_seq'),
                'rhel_7','Red Hat Enterprise Linux 7'
        );
