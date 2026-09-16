-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnOrgConfiguration ADD errata_emails_enabled CHAR(1)
    DEFAULT('Y') NOT NULL
    CONSTRAINT rhn_org_conf_errata_emails_chk
    CHECK (errata_emails_enabled in ('Y', 'N'));
