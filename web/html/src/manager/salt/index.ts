/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export default {
  "salt/cmd/remote-commands": () => import("./cmd/remote-commands"),
  "salt/formula-catalog/org-formula-catalog": () => import("./formula-catalog/org-formula-catalog.renderer"),
  "salt/formula-catalog/org-formula-details": () => import("./formula-catalog/org-formula-details"),
  "salt/keys/key-management": () => import("./keys/key-management"),
};
