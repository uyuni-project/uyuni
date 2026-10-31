-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM rhnserverlock sl
	WHERE sl.server_id IN (SELECT sm.server_id FROM suseminioninfo sm);

