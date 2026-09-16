-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

delete from rhnFeature where label = 'ftr_schedule_probe' or label = 'ftr_probes';
