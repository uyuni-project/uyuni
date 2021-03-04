import * as React from 'react';
import { NetsEdit } from './nets-edit';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, { serverId, actionChains, timezone, localTime, netName }) => {
  SpaRenderer.renderNavigationReact(
    <NetsEdit
      serverId={serverId}
      netName={netName}
      actionChains={actionChains}
      timezone={timezone}
      localTime={localTime}
    />,
    document.getElementById(id),
  );
};

