-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnUserGroupMembers ADD temporary CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_ugmembers_t_ck
        CHECK (temporary in ('Y', 'N'));

CREATE UNIQUE INDEX rhn_ugmembers_uid_ugid_temp_uq
    ON rhnUserGroupMembers (user_id, user_group_id, temporary);
DROP INDEX rhn_ugmembers_uid_ugid_uq;
