-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnUserExtGroup ADD org_id NUMERIC DEFAULT NULL
    CONSTRAINT rhn_userExtGroup_oid_fk
        REFERENCES web_customer (id)
        ON DELETE CASCADE;

CREATE UNIQUE INDEX rhn_userextgroup_label_oid_uq
    ON rhnUserExtGroup (label, org_id) ;
DROP INDEX rhn_userextgroup_label_uq;
