/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import AccessGroup, { AccessGroupPropsType } from "./access-group";

export const renderer = (id: string, accessGroup: AccessGroupPropsType) => {
  SpaRenderer.renderNavigationReact(<AccessGroup accessGroup={accessGroup} />, document.getElementById(id));
};
