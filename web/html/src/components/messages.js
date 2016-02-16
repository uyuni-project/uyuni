'use strict';

var React = require("react");

var Messages = React.createClass({
  _classNames : {
    "error": "danger",
    "success": "success",
    "info": "info",
    "warning": "warning",
  },

  getInitialState: function() {
    return {};
  },

  render: function() {
    var msgs = this.props.items.map(function(item) {
                return (<div className={'alert alert-' + this._classNames[item.severity]}>{item.text}</div>);
            }.bind(this));
    return (<div>{msgs}</div>);
  }

});

module.exports = {
    Messages : Messages
}
