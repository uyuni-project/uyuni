import * as React from 'react';
import { NetsCreate } from './nets-create';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, actionChains, timezone, localTime }) => {
  SpaRenderer.renderNavigationReact(
    <NetsCreate
      serverId={serverId}
      actionChains={actionChains}
      timezone={timezone}
      localTime={localTime}
    />,
    document.getElementById(id),
  );
};

