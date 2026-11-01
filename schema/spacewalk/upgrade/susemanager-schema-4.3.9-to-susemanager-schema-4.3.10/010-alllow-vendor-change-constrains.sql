-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- take care that the check exists. We might have lost it in the past
ALTER TABLE rhnActionDup DROP CONSTRAINT IF EXISTS rhn_actiondup_avc_ck;
ALTER TABLE rhnActionDup ADD CONSTRAINT rhn_actiondup_avc_ck CHECK (allow_vendor_change in ('Y', 'N'));
