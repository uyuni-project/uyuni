-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnchannel SET end_of_life = null WHERE end_of_life <= TO_TIMESTAMP(3600*24);
