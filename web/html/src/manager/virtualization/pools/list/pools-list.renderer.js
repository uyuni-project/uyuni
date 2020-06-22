import * as React from 'react';
import { PoolsList } from './pools-list';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, pageSize, hypervisor }) => {
  SpaRenderer.renderNavigationReact(
    <PoolsList
      pageSize={pageSize}
      serverId={serverId}
      hypervisor={hypervisor}
    />,
    document.getElementById(id),
  );
};
