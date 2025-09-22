import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";

import FormulaCatalog from "./org-formula-catalog";

type RendererProps = {
  flashMessage?: ServerMessageType;
  warningMessage?: ServerMessageType;
};

export const renderer = (id: string, { flashMessage, warningMessage }: RendererProps = {}) => {
  SpaRenderer.renderNavigationReact(
    <FormulaCatalog flashMessage={flashMessage} warningMessage={warningMessage} />,
    document.getElementById(id)
  );
};
