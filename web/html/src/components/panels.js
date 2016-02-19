'use strict';

var React = require("react");

class InnerPanel extends React.Component {

  constructor(props) {
    super();
  }

  render() {
    return (
      <div>
        <h2>
          <i className={"fa " +  this.props.icon}></i>
          {this.props.title}
          <span className="btn-group pull-right">
              {this.props.buttons}
          </span>
        </h2>
        <div className="row col-md-12">
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
    InnerPanel: InnerPanel,
    PanelRow: PanelRow
}