// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

class ActivationKeyChannels extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
    }
  }

  render() {
    return (
      <div>placeholder</div>
    )
  }
}

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

ReactDOM.render(
  <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : ''} />,
  document.getElementById(customValues.DOMid)
);