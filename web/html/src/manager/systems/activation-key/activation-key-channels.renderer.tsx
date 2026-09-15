/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import ActivationKeyChannels from "./activation-key-channels";

type RendererProps = {
  activationKeyId?: any;
};

export const renderer = (id: string, { activationKeyId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(
    <ActivationKeyChannels activationKeyId={activationKeyId} />,
    document.getElementById(id)
  );
