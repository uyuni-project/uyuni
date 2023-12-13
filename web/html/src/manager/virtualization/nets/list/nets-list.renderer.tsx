import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { NetsList } from "./nets-list";

type RendererProps = {
  serverId: string;
  hypervisor: string;
  support_enhanced_network: boolean;
};

export const renderer = (id: string, { serverId, hypervisor, support_enhanced_network }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <NetsList serverId={serverId} hypervisor={hypervisor} allow_changing={support_enhanced_network} />,
    document.getElementById(id)
  );
};
