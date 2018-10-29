/* eslint-disable */
import ActivationKeyChannels from './activation-key-channels';
import React from 'react';
import ReactDOM from 'react-dom';

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

ReactDOM.render(
    <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : -1} />,
    document.getElementById(customValues.DOMid)
);
