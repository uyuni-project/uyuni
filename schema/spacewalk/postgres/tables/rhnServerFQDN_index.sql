-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- function index for rhnServerFQDN

CREATE UNIQUE INDEX rhn_srv_fqdn_prim_fqdn
  ON rhnServerFQDN
  (server_id) where is_primary = 'Y';
