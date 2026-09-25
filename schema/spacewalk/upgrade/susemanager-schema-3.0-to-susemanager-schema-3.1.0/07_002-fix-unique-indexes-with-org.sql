-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop index if exists rhn_cs_label_uq;
CREATE UNIQUE INDEX rhn_cs_label_uq
    ON rhnContentSource(COALESCE(org_id, 0), label)
    ;
drop index if exists rhn_cs_repo_uq;
CREATE UNIQUE INDEX rhn_cs_repo_uq
    ON rhnContentSource(COALESCE(org_id, 0), type_id, source_url)
    ;
