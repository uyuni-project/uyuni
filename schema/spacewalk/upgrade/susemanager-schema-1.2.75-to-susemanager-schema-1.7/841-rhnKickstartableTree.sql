-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnKickstartableTree DROP CONSTRAINT rhn_kstree_cid_fk;
ALTER TABLE rhnKickstartableTree ADD CONSTRAINT rhn_kstree_cid_fk FOREIGN KEY (channel_id) REFERENCES rhnChannel (id) ON DELETE CASCADE;
