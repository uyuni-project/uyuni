-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM access.endpointNamespace
WHERE endpoint_id IN (SELECT id FROM access.endpoint WHERE endpoint = '/manager/api/system/registerPeripheralServer');

DELETE FROM access.endpoint
WHERE endpoint = '/manager/api/system/registerPeripheralServer';
