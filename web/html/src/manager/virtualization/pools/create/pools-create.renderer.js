import * as React from 'react';
import { PoolsCreate } from './pools-create';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, actionChains, timezone, localTime }) => {
  SpaRenderer.renderNavigationReact(
    <PoolsCreate
      serverId={serverId}
      actionChains={actionChains}
      timezone={timezone}
      localTime={localTime}
    />,
    document.getElementById(id),
  );
};
