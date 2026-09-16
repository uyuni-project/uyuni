/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import CreateAccessGroup from "./access-group";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<CreateAccessGroup />, document.getElementById(id));
};
