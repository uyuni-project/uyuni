import SpaRenderer from 'core/spa/spa-renderer';
import * as React from 'react';
import { GuestsList } from './guests-list';

export const renderer = (id, { serverId, pageSize, saltEntitled, foreignEntitled, isAdmin, hypervisor }) => {
  SpaRenderer.renderNavigationReact(
    <GuestsList
      serverId={serverId}
      pageSize={pageSize}
      saltEntitled={saltEntitled}
      foreignEntitled={foreignEntitled}
      isAdmin={isAdmin}
      hypervisor={hypervisor}
    />,
    document.getElementById(id),
  );
};
