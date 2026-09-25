-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseProducts ADD base CHAR(1) DEFAULT ('N') NOT NULL;
