import * as React from 'react';
import { PoolsEdit } from './pools-edit';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, actionChains, timezone, localTime, poolName }) => {
  SpaRenderer.renderNavigationReact(
    <PoolsEdit
      serverId={serverId}
      poolName={poolName}
      actionChains={actionChains}
      timezone={timezone}
      localTime={localTime}
    />,
    document.getElementById(id),
  );
};

