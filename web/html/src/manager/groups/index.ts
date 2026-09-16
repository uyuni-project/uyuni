/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export default {
  "groups/formula/group-formula": () => import("./formula/group-formula.renderer"),
  "groups/formula/group-formula-selection": () => import("./formula/group-formula-selection.renderer"),
  "groups/config-channels/group-config-channels": () => import("./config-channels/group-config-channels"),
};
