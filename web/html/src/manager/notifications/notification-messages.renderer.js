const { NotificationMessages } = require('./notification-messages');
const ReactDOM = require("react-dom");
const React = require("react");

ReactDOM.render(
  <NotificationMessages />,
    document.getElementById("notification-messages")
);
