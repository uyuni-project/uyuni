import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { PoolsList } from "./pools-list";

type RendererProps = {
  serverId: string;
  hypervisor: string;
};

export const renderer = (id: string, { serverId, hypervisor }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <PoolsList serverId={serverId} hypervisor={hypervisor} />,
    document.getElementById(id)
  );
};
