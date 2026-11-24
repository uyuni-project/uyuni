-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM access.endpoint
    WHERE endpoint = '/manager/storybook'
    AND http_method = 'GET'
    AND scope = 'W';
