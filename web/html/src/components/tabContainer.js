'use strict';

var React = require("react");

var TabContainer = React.createClass({
  getInitialState: function() {
    return {"tab" : 0};
  },

  show: function(tabIndex) {
    this.setState({"tab": tabIndex});
  },

  render: function() {
    var container = this;

    var tabLabels = this.props.labels.map(function(label, index) {
      return (<TabLabel onClick={container.show} tab={index} text={label} active={index == container.state.tab} />);
    });

    var tabPanels = (this.props.panels != null && this.props.panels.length > 0 ? this.props.panels[this.state.tab] : t("Loading..."));

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
  onClick: function() {
    this.props.onClick(this.props.tab);
  },

  render: function() {
    return(
      <li className={this.props.active ? "active" : ""}>
        <a href="#" onClick={this.onClick}>{this.props.text}</a>
      </li>
    );
  }
});

module.exports = {
    TabContainer : TabContainer,
    TabLabel : TabLabel
}
