import * as React from "react";
import { SubscribeChannels } from "./subscribe-channels";
import SpaRenderer from "core/spa/spa-renderer";

type RendererProps = {
  systemId?: any;
};

export const renderer = (id, { systemId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SubscribeChannels serverId={systemId} />, document.getElementById(id));
