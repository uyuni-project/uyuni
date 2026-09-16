-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE TYPE billing_dimension_t AS ENUM (
    'managed_systems',
    'monitoring'
);
