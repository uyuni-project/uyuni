import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SubscribeChannels } from "./subscribe-channels";

type RendererProps = {
  systemId?: any;
};

export const renderer = (id, { systemId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SubscribeChannels serverId={systemId} />, document.getElementById(id));
