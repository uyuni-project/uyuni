/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import AccessGroupList from "./access-group-list";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<AccessGroupList />, document.getElementById(id));
};
