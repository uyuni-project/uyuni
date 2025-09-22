import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SupportData, UploadRegionArray } from "./support-data";

type RendererProps = {
  serverId: number;
  availableRegions: UploadRegionArray;
  programName: string;
};

export const renderer = (id: string, { serverId, availableRegions, programName }: RendererProps): void => {
  SpaRenderer.renderNavigationReact(
    <SupportData serverId={serverId} availableRegions={availableRegions} supportProgramName={programName} />,
    document.getElementById(id)
  );
};
