import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { GuestsList } from "./guests-list";

type RendererProps = {
  serverId: string;
  pageSize: number;
  saltEntitled: boolean;
  foreignEntitled: boolean;
  isAdmin: boolean;
  hypervisor: string;
};

export const renderer = (id: string, { serverId, pageSize, saltEntitled, foreignEntitled, isAdmin, hypervisor }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <GuestsList
      serverId={serverId}
      pageSize={pageSize}
      saltEntitled={saltEntitled}
      foreignEntitled={foreignEntitled}
      isAdmin={isAdmin}
      hypervisor={hypervisor}
    />,
    document.getElementById(id)
  );
};
