/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export default {
  "notifications/notifications": () => import("./notifications"),
  "notifications/notifications-list": () => import("./notifications-list.renderer"),
};
