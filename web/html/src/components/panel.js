'use strict';

var React = require("react")
var t = require("./translation")

var PanelButton = React.createClass({

  getInitialState: function() {
    return {};
  },

  render: function() {
      return (
          <div className="spacewalk-toolbar">
            <a href={this.props.action}>
              <i className={'fa ' + this.props.icon}></i>
              {t(this.props.text)}
            </a>
          </div>
        )
  }
});

var Panel = React.createClass({
  getInitialState: function() {
    return {};
  },

  render: function() {
    return (
      <div>
        <div className="spacewalk-toolbar-h1">
          {this.props.button}
          <h1><i className={'fa ' + this.props.icon}></i>{t(this.props.title)}</h1>
        </div>
        {this.props.children}
      </div>
    );
  }
});

module.exports = {
    Panel : Panel,
    PanelButton : PanelButton
}