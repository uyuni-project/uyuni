import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import ActivationKeyChannels from "./activation-key-channels";

type RendererProps = {
  activationKeyId?: any;
};

export const renderer = (parent: Element, { activationKeyId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<ActivationKeyChannels activationKeyId={activationKeyId} />, parent);
