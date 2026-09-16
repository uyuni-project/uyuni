/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { ProxyConfig } from "./container-config";

export const renderer = (id: string, noSSL: boolean) => {
  return SpaRenderer.renderNavigationReact(<ProxyConfig noSSL={noSSL} />, document.getElementById(id));
};
