/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheral from "./peripherals";

export const renderer = (id: string, flashMessage: string) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral flashMessage={flashMessage} />, document.getElementById(id));
};
