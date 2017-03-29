"use strict";

const React = require("react");

const TabContainer = React.createClass({
  propTypes: {
    labels: React.PropTypes.arrayOf(React.PropTypes.node).isRequired,
    hashes: React.PropTypes.arrayOf(React.PropTypes.string).isRequired, // must start with #
    tabs: React.PropTypes.arrayOf(React.PropTypes.node).isRequired,
    initialActiveTabHash: React.PropTypes.string,
    onTabHashChange: React.PropTypes.func, // takes a hash parameter
  },

  getInitialState: function() {
    return {activeTabHash: this.sanitizeHash(this.props.initialActiveTabHash)};
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({activeTabHash: this.sanitizeHash(nextProps.initialActiveTabHash, nextProps.hashes)});
  },

  sanitizeHash: function(hash, hashArr) {
    hashArr = hashArr || this.props.hashes;

    if (hashArr.indexOf(hash) >= 0) {
      return hash;
    }
    return hashArr[0];
  },

  onActiveTabChange: function(hash, event) {
    event.preventDefault();

    this.setState({activeTabHash: hash});
    if (this.props.onTabHashChange) {
      this.props.onTabHashChange(hash);
    }
  },

  render: function() {
    const labels = this.props.hashes.map((hash, i) => {
      const label = this.props.labels[i];
      return <TabLabel onClick={(event) => this.onActiveTabChange(hash, event)} text={label} active={this.state.activeTabHash == hash} hash={hash} key={hash} />;
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

const TabLabel = (props) =>
  <li className={props.active ? "active" : ""}>
    <a href={props.hash} onClick={props.onClick}>{props.text}</a>
  </li>
;

module.exports = {
    TabContainer : TabContainer
}
