import React from 'react';
import { SubscribeChannels } from './subscribe-channels';
import SpaRenderer from 'core/spa/spa-renderer';

export const renderer = (id, {systemId}) => SpaRenderer.renderNavigationReact(
  <SubscribeChannels serverId={systemId} />,
  document.getElementById(id),
);
