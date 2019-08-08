/* eslint-disable */
import ActivationKeyChannels from './activation-key-channels';
import React from 'react';
import ReactDOM from 'react-dom';
import SpaRenderer from "core/spa/spa-renderer";

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

SpaRenderer.renderNavigationReact(
    <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : -1} />,
    document.getElementById(customValues.DOMid)
);
