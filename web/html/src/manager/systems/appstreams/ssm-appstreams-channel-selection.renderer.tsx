import { SSMAppStreamChannel } from "manager/appstreams/appstreams.type";

import SpaRenderer from "core/spa/spa-renderer";

import { AppStreamsChannelSelection } from "./ssm-appstreams-channel-selection";

type RendererProps = { channels: SSMAppStreamChannel[] };

export const renderer = (id: string, { channels }: RendererProps) =>
  SpaRenderer.renderNavigationReact(<AppStreamsChannelSelection channels={channels} />, document.getElementById(id));
