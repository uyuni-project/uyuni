/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export default {
  "images/image-build": () => import("./image-build"),
  "images/image-import": () => import("./image-import"),
  "images/image-profile-edit": () => import("./image-profile-edit"),
  "images/image-profiles": () => import("./image-profiles"),
  "images/image-store-edit": () => import("./image-store-edit"),
  "images/image-stores": () => import("./image-stores"),
  "images/image-view": () => import("./image-view"),
};
