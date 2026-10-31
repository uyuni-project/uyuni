/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import AppStreams from "./activation-keys-appstreams";

type RendererProps = {
  channelsAppStreams?: string;
};

export const renderer = (id: string, { channelsAppStreams }: RendererProps) => {
  let channelsAppStreamsJson: any[] = [];
  try {
    channelsAppStreamsJson = JSON.parse(channelsAppStreams || "");
  } catch (error) {
    Loggerhead.error(error);
  }
  SpaRenderer.renderNavigationReact(
    <AppStreams channelsAppStreams={channelsAppStreamsJson} />,
    document.getElementById(id)
  );
};
