-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseContentProject
  ADD CONSTRAINT suse_ct_project_fenvid_fk
  FOREIGN KEY (first_env_id)
  REFERENCES suseContentEnvironment(id)
  ON DELETE SET NULL;
