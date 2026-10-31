/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import AccessGroupList from "./access-group-list";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<AccessGroupList />, document.getElementById(id));
};
