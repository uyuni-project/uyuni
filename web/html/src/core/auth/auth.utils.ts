/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { rolesType } from "./roles-context";

export function isOrgAdmin(roles: rolesType) {
  return roles.includes("org_admin");
}
