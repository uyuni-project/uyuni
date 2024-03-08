import SpaRenderer from "core/spa/spa-renderer";

import Appstreams from "./appstreams";

type RendererProps = {
  channelsModules?: string;
};

export const renderer = (id: string, { channelsModules }: RendererProps) => {
  let channelsModulesJson: any[] = [];
  try {
    channelsModulesJson = JSON.parse(channelsModules || "");
  } catch (error) {
    Loggerhead.error(error);
  }
  SpaRenderer.renderNavigationReact(<Appstreams channelsModules={channelsModulesJson} />, document.getElementById(id));
};
