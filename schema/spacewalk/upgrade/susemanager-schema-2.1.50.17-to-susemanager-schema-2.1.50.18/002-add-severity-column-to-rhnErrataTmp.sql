-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

--
-- Adding the missing columnt 'severity_id' to rhnErrataTmp
-- table to be consistent with rhnErrata table

ALTER TABLE rhnerratatmp
   ADD severity_id NUMERIC;
