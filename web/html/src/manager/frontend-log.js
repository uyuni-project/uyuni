/* eslint-disable */
"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const Loggerhead = require("loggerhead-module").create({ url: "/rhn/manager/frontend-log" });

const FrontendLog = React.createClass({
  componentWillMount: function() {
    Loggerhead.setHeaders = function(headers) {
      headers.set('X-CSRF-Token', document.getElementsByName('csrf_token')[0].value);
      return headers;
    }
  },

  render: function() {
    return null;
  }
});

ReactDOM.render(
  <FrontendLog />,
  document.getElementById('frontend-log')
);
