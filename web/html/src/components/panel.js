/* eslint-disable */
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

function BootstrapPanel(props) {
    return (
        <div className="panel panel-default">
            { props.title &&
            <div className="panel-heading">
                <h2>
                  { props.icon && <i className={"fa " +  props.icon}></i> }
                  {props.title}
                </h2>
                { props.header && <span>{props.header}</span>}
            </div>
            }
            <div className="panel-body">
                { props.children }
            </div>
            { props.footer &&
              <div className="panel-footer">
                {props.footer}
              </div>
            }
        </div>
    );
}

function SmallPanel(props) {
    return (
        <div className="panel panel-default">
            { props.title &&
            <div className="panel-heading">
                <h4>
                  { props.icon && <i className={"fa " + props.icon}></i> }
                  {props.title}
                  { props.editUrl && <span> (<a href={props.editUrl}>{t("Edit")}</a>)</span> }
                </h4>
            </div>
            }
            <div className="panel-body">
                { props.children }
            </div>
        </div>
    );
}

module.exports = {
    PanelButton : PanelButton,
    PanelRow : PanelRow,
    BootstrapPanel : BootstrapPanel,
    SmallPanel: SmallPanel
}
