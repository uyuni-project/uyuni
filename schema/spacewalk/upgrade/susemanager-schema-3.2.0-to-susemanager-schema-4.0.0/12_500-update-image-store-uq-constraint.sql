-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DROP INDEX IF EXISTS suse_imgstore_label_uq;
DROP INDEX IF EXISTS suse_imgstore_oid_label_uq;

CREATE UNIQUE INDEX suse_imgstore_oid_label_uq
    ON suseImageStore (org_id, label);
