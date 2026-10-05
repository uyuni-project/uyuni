-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnCompsType
  SELECT 3, 'mediaproducts'
   WHERE NOT EXISTS ( SELECT 1 FROM rhnCompsType WHERE label = 'mediaproducts');

