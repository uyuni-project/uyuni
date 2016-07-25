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
    var msgs = this.props.items.map(function(item, index) {
                return (<div key={"msg" + index} className={'alert alert-' + this._classNames[item.severity]}>{item.text}</div>);
            }.bind(this));
    return (<div key={"messages-pop-up"}>{msgs}</div>);
  }

});

function msg(severityIn, textIn) {
    return [{severity: severityIn, text: textIn}];
}

module.exports = {
    Messages : Messages,
    Utils: {
        info: function (textIn) {
            return msg("info", textIn);
        },
        success: function (textIn) {
            return msg("success", textIn);
        },
        warning: function (textIn) {
            return msg("warning", textIn);
        },
        error: function (textIn) {
            return msg("error", textIn);
        }
    }
}
