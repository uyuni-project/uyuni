-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- rhnFeature

DELETE FROM rhnFeature
  WHERE label = 'ftr_nonlinux_support';
