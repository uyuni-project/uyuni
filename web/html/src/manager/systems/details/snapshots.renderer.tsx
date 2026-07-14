import SpaRenderer from "core/spa/spa-renderer";

import Snapshots from "./snapshots";

type RendererProps = {
  snapshots?: string;
};

export const renderer = (id: string, { snapshots }: RendererProps) => {
  let snapshotsJson: any[] = [];
  try {
    snapshotsJson = JSON.parse(snapshots || "[]");
  } catch (error) {
    Loggerhead.error(error);
  }
  SpaRenderer.renderNavigationReact(<Snapshots snapshots={snapshotsJson} />, document.getElementById(id));
};
