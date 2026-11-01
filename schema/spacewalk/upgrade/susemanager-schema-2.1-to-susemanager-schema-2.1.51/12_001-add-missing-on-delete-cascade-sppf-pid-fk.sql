-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE susePackageProductFile DROP CONSTRAINT sppf_pid_fk;
ALTER TABLE susePackageProductFile ADD CONSTRAINT sppf_pid_fk FOREIGN KEY (package_id) REFERENCES rhnPackage(id) ON DELETE CASCADE;
