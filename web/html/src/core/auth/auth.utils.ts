/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { rolesType } from "./roles-context";

export function isOrgAdmin(roles: rolesType) {
  return roles.includes("org_admin");
}
