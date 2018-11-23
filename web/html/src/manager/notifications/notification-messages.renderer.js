const React = require('react');
const ReactDOM = require('react-dom');
const { NotificationMessages } = require('./notification-messages');

ReactDOM.render(
  <NotificationMessages />,
  document.getElementById('notification-messages'),
);
