import * as React from 'react';
import { NetsList } from './nets-list';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, pageSize, hypervisor }) => {
  SpaRenderer.renderNavigationReact(
    <NetsList
      serverId={serverId}
      pageSize={pageSize}
      hypervisor={hypervisor}
    />,
    document.getElementById(id),
  );
};
