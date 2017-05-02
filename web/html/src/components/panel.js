'use strict';

// TODO rename this to panels.js
// TODO use the same syntax (ES5 or ES6) in all classes

var React = require("react");

var PanelButton = React.createClass({

  getInitialState: function() {
    return {};
  },

  render: function() {
      var href = this.props.action ? this.props.action : "#";

      return (
          <div className="spacewalk-toolbar">
            <a id={this.props.id} href={this.props.action} onClick={this.props.handler}>
              <i className={'fa ' + this.props.icon}></i>
              {t(this.props.text)}
            </a>
          </div>
        )
  }
});

// TODO rename this to TopPanel
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

class InnerPanel extends React.Component {

  constructor(props) {
    super();
  }

  render() {
    return (
      <div className="col-md-12">
        <h2>
          <i className={"fa " +  this.props.icon}></i>
          {this.props.title}
        </h2>
        <div className="spacewalk-section-toolbar">
          <div className="btn-group action-button-wrapper">
            {this.props.buttons}
          </div>
        </div>
        <div className="row">
          <div className="panel panel-default">
            <div className="panel-body">
                {this.props.children}
            </div>
          </div>
        </div>
      </div>
    );
  }

}

class PanelRow extends React.Component {

  constructor(props) {
    super();
  }

  render() {
    return (
        <div className="row">
            <span className="col-md-8 pull-right">
                <span className={this.props.className}>
                    {this.props.children}
                </span>
            </span>
        </div>
    );
  }

}

module.exports = {
    Panel : Panel,
    PanelButton : PanelButton,
    InnerPanel : InnerPanel,
    PanelRow : PanelRow
}
