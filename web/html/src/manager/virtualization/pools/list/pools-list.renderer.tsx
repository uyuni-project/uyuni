import * as React from "react";
import { PoolsList } from "./pools-list";
import SpaRenderer from "core/spa/spa-renderer";

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
