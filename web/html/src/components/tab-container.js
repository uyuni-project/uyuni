"use strict";

var React = require("react");

var TabContainer = React.createClass({
  getInitialState: function() {
    return {activeTabHash: this.sanitizeHash(this.props.initialActiveTabHash)};
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({activeTabHash: this.sanitizeHash(nextProps.initialActiveTabHash)});
  },

  sanitizeHash: function(hash) {
    if (this.props.hashes.indexOf(hash) >= 0) {
      return hash;
    }
    return this.props.hashes[0];
  },

  onActiveTabChange: function(hash) {
    this.setState({activeTabHash: hash});
    if (this.props.onTabHashChange) {
      this.props.onTabHashChange(hash);
    }
  },

  render: function() {
    const labels = this.props.hashes.map((hash, i) => {
      const label = this.props.labels[i];
      return <TabLabel onClick={() => this.onActiveTabChange(hash)} text={label} active={this.state.activeTabHash == hash} hash={hash} />;
    });

    const tab = this.props.tabs[this.props.hashes.indexOf(this.state.activeTabHash)];

    return (
      <div>
        <div className="spacewalk-content-nav">
          <ul className="nav nav-tabs">
            {labels}
          </ul>
        </div>
        {tab}
      </div>
    );
  }
});

var TabLabel = (props) =>
  <li className={props.active ? "active" : ""}>
    <a href={props.hash} onClick={props.onClick}>{props.text}</a>
  </li>
;

module.exports = {
    TabContainer : TabContainer,
    TabLabel : TabLabel
}
