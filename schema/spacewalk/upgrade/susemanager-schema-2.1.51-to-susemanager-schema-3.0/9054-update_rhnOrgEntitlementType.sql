-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM rhnOrgEntitlements
  WHERE entitlement_id IN (
    SELECT id FROM rhnOrgEntitlementType WHERE label = 'rhn_provisioning'
  );

DELETE FROM rhnOrgEntitlementType
  WHERE label = 'rhn_provisioning';
