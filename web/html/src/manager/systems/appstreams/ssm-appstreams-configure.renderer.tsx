import { ChannelAppStream } from "manager/appstreams/appstreams.type";

import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";

import { SSMAppStreamsConfigure } from "./ssm-appstreams-configure";

type RendererProps = {
  actionChains: ActionChain[];
  appstreams: ChannelAppStream;
};

export const renderer = (id: string, { appstreams }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <SSMAppStreamsConfigure channelAppStreams={appstreams} />,
    document.getElementById(id)
  );
