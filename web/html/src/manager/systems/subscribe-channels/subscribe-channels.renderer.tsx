/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { SubscribeChannels } from "./subscribe-channels";

type RendererProps = {
  systemId?: any;
};

export const renderer = (id, { systemId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SubscribeChannels serverId={systemId} />, document.getElementById(id));
