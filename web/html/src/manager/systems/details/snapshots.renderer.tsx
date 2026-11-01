/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import Snapshots from "./snapshots";

type RendererProps = {
  serverId: string;
};

export const renderer = (id: string, { serverId }: RendererProps) => {
  SpaRenderer.renderNavigationReact(<Snapshots serverId={serverId} />, document.getElementById(id));
};
