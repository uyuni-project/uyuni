/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
export default {
  "content-management/create-project": () => import("./create-project/create-project.renderer"),
  "content-management/list-filters": () => import("./list-filters/list-filters.renderer"),
  "content-management/list-projects": () => import("./list-projects/list-projects.renderer"),
  "content-management/project": () => import("./project/project.renderer"),
};
