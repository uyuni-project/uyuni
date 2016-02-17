'use strict';

var React = require("react");

var TabContainer = React.createClass({
  getInitialState: function() {
    return {"tabIndex" : 0};
  },

  show: function(tabIndex) {
    this.setState({"tabIndex": tabIndex});
  },

  render: function() {
    var tabLabels = this.props.labels.map((label, tabIndex) =>
      <TabLabel onClick={() => this.show(tabIndex)} text={label} active={tabIndex == this.state.tabIndex} />
    );

    var tabPanels = (this.props.panels != null && this.props.panels.length > 0 ? this.props.panels[this.state.tabIndex] : t("Loading..."));

    return (
      <div>
        <div className="spacewalk-content-nav">
          <ul className="nav nav-tabs">
            {tabLabels}
          </ul>
        </div>
        {tabPanels}
      </div>
    );
  }
});

var TabLabel = React.createClass({
  render: function() {
    return(
      <li className={this.props.active ? "active" : ""}>
        <a href="#" onClick={this.props.onClick}>{this.props.text}</a>
      </li>
    );
  }
});

module.exports = {
    TabContainer : TabContainer,
    TabLabel : TabLabel
}
