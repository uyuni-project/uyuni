import * as React from "react";
import ActivationKeyChannels from "./activation-key-channels";
import SpaRenderer from "core/spa/spa-renderer";

type RendererProps = {
  activationKeyId?: any;
};

export const renderer = (id: string, { activationKeyId }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(
    <ActivationKeyChannels activationKeyId={activationKeyId} />,
    document.getElementById(id)
  );
