-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- function index for rhnServerNetInterface

CREATE UNIQUE INDEX rhn_srv_net_iface_prim_iface
  ON rhnServerNetInterface
  (server_id) where is_primary is not null;
