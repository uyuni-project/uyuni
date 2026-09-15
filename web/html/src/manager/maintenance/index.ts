/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export default {
  "maintenance/maintenance-windows": () => import("./maintenance-windows"),
  "maintenance/system-assignment": () => import("./ssm/system-assignment"),
};
