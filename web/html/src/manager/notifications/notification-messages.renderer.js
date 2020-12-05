import SpaRenderer from 'core/spa/spa-renderer';
import React from 'react';
import { NotificationMessages } from './notification-messages';

export const renderer = () => SpaRenderer.renderNavigationReact(
  <NotificationMessages />,
  document.getElementById('notification-messages'),
);
