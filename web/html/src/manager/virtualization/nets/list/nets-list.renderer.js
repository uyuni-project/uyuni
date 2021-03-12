import * as React from 'react';
import { NetsList } from './nets-list';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, pageSize, hypervisor, support_enhanced_network }) => {
  SpaRenderer.renderNavigationReact(
    <NetsList
      serverId={serverId}
      pageSize={pageSize}
      hypervisor={hypervisor}
      allow_changing={support_enhanced_network}
    />,
    document.getElementById(id),
  );
};
