/* eslint-disable */
import ActivationKeyChannels from './activation-key-channels';
import React from 'react';
import ReactDOM from 'react-dom';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, {activationKeyId}) => SpaRenderer.renderNavigationReact(
    <ActivationKeyChannels activationKeyId={activationKeyId} />,
    document.getElementById(id)
);
