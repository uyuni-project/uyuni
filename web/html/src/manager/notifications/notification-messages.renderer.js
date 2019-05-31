const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const ReactDOM = require('react-dom');
const { NotificationMessages } = require('./notification-messages');

SpaRenderer.renderNavigationReact(
  <NotificationMessages />,
  document.getElementById('notification-messages'),
);
