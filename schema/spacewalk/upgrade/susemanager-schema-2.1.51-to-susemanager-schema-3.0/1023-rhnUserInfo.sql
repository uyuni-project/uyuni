-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnUserInfo ADD tasko_notify CHAR(1)
      DEFAULT('Y') NOT NULL
      CONSTRAINT rhn_user_info_tasko_ck
        CHECK (tasko_notify in ('Y', 'N'));
