import * as React from 'react';
import { PoolsList } from './pools-list';
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.pools = window.pageRenderers.pools || {};
window.pageRenderers.pools.list = window.pageRenderers.pools.list || {};
window.pageRenderers.pools.list.renderer = (id, { serverId, pageSize }) => {
  SpaRenderer.renderNavigationReact(
    <PoolsList
      refreshInterval={5 * 1000}
      pageSize={pageSize}
      serverId={serverId}
    />,
    document.getElementById(id),
  );
};
