/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import MonitoringAdmin from "./monitoring-admin";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<MonitoringAdmin />, document.getElementById(id));
};
