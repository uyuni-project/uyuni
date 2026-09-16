-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseMatcherRunData
RENAME COLUMN unmatchedSystemReportBinary TO unmatchedProductReportBinary;

