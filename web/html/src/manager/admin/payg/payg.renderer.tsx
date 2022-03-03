import * as React from "react";

import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import Pyag from "./payg";

type RendererProps = {
  payg?: string;
  wasFreshlyCreatedMessage?: string;
};

export const renderer = (id: string, { payg, wasFreshlyCreatedMessage }: RendererProps = {}) => {
  let paygJson: any = {};
  try {
    paygJson = JSON.parse(payg || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <Pyag payg={paygJson} wasFreshlyCreatedMessage={wasFreshlyCreatedMessage} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
