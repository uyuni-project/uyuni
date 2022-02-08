import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr/toastr";

import PackageStates from "./package-states";

type RendererProps = {
  serverId?: any;
};

export const renderer = (id: string, { serverId }: RendererProps = {}) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <PackageStates serverId={serverId} />
    </>,
    document.getElementById(id)
  );
};
