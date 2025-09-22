import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SubscribeChannels } from "./subscribe-channels";

interface RendererProps {
  systemId?: any;
}

export const renderer = (id, { systemId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SubscribeChannels serverId={systemId} />, document.getElementById(id));
