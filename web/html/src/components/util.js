"use strict";

const React = require("react");

// use this mixin to persist an element's state between mounts
const StatePersistedMixin = {
  propTypes: {
    saveState: React.PropTypes.func, // is called before unmount
    loadState: React.PropTypes.func, // is called on mount
  },

  componentWillUnmount: function() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  },

  componentWillMount: function() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
  },
};

module.exports = {
    StatePersistedMixin : StatePersistedMixin
}
