import SpaRenderer from "core/spa/spa-renderer";

import Appstreams from "./appstreams";

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
    <Appstreams channelsAppStreams={channelsAppStreamsJson} />,
    document.getElementById(id)
  );
};
