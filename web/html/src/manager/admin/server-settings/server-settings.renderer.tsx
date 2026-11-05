/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import ServerSettings from "./server-settings";
import { ServerSetting } from "./server-settings.types";

export const renderer = (id: string, settings: ServerSetting[]) => {
  SpaRenderer.renderNavigationReact(<ServerSettings settings={settings} />, document.getElementById(id));
};
