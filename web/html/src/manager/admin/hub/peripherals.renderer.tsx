/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheral from "./peripherals";

export const renderer = (id: string, flashMessage: string) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral flashMessage={flashMessage} />, document.getElementById(id));
};
