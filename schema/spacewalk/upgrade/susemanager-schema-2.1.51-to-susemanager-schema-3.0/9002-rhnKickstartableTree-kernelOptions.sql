-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnKickstartableTree
    ADD kernel_options       VARCHAR(256);
ALTER TABLE rhnKickstartableTree
    ADD kernel_options_post  VARCHAR(256);
