/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import { Proxy } from "./proxy";

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <Proxy proxies={window.proxies} currentProxy={window.currentProxy} />,
    document.getElementById(id)
  );
