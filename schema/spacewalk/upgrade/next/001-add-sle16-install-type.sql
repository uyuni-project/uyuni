--
-- Copyright (c) 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only
--

INSERT INTO rhnKSInstallType (id, label, name)
SELECT sequence_nextval('rhn_ksinstalltype_id_seq'), 'opensuse16.1','SUSE Linux Enterprise 16'
ON CONFLICT (label) DO NOTHING;
