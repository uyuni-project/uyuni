/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import { SubscribeChannels } from "./subscribe-channels";

type RendererProps = {
  systemId?: any;
};

export const renderer = (id, { systemId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SubscribeChannels serverId={systemId} />, document.getElementById(id));
