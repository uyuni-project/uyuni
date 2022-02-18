import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { PoolsList } from "./pools-list";

type RendererProps = {
  serverId: string;
  pageSize: number;
  hypervisor: string;
};

export const renderer = (id: string, { serverId, pageSize, hypervisor }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <PoolsList pageSize={pageSize} serverId={serverId} hypervisor={hypervisor} />,
    document.getElementById(id)
  );
};
