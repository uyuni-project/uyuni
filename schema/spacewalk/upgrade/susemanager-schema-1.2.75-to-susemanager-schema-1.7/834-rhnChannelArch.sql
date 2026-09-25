-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnChannelArch
    ADD CONSTRAINT rhn_carch_name_uq UNIQUE (name)
    USING INDEX TABLESPACE [[2m_tbs]];
