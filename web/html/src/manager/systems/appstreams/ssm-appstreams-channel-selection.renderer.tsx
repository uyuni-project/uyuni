/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { SSMAppStreamChannel } from "manager/appstreams/appstreams.type";

import SpaRenderer from "core/spa/spa-renderer";

import { AppStreamsChannelSelection } from "./ssm-appstreams-channel-selection";

type RendererProps = { channels: SSMAppStreamChannel[] };

export const renderer = (id: string, { channels }: RendererProps) =>
  SpaRenderer.renderNavigationReact(<AppStreamsChannelSelection channels={channels} />, document.getElementById(id));
