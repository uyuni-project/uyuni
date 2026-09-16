-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX wupi_user_id_idx
    ON web_user_personal_info (web_user_id);
