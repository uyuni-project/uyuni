const ReactDOM = require('react-dom');
const React = require('react');
const { NotificationMessages } = require('./notification-messages');

ReactDOM.render(
  <NotificationMessages />,
  document.getElementById('notification-messages'),
);
