const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const { NotificationMessages } = require('./notification-messages');

export const renderer = () => SpaRenderer.renderNavigationReact(
  <NotificationMessages />,
  document.getElementById('notification-messages'),
);
