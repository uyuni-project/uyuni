-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnSsmOperationServer
  ADD note VARCHAR(256)
    DEFAULT NULL
;
