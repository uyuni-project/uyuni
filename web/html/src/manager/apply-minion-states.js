'use strict';

const React = require("react");
const Buttons = require("../components/buttons");

const AsyncButton = Buttons.AsyncButton;

class ApplyState extends React.Component {

  constructor(props) {
    super();
    this.state = {
    };
  }

  applySaltState() {

  }

  render() {
    return (
      <div>
        <h2>
          <i className="fa spacewalk-icon-package-add"></i>
          {t("Apply States")}
          <span className="btn-group pull-right">
              <AsyncButton action={this.applySaltState} name={t("Apply")} />
          </span>
        </h2>
        <div className="row col-md-12">
          <div className="panel panel-default">
            <div className="panel-body">

            </div>
          </div>
        </div>
      </div>
    );
  }

}

React.render(
  <ApplyState/>,
  document.getElementById('apply-states')
);