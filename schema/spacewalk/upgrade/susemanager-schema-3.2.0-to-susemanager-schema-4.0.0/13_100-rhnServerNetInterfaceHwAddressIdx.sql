-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX rhn_srv_net_iface_hw_addr_idx
    ON rhnServerNetInterface (hw_addr)
    ;

