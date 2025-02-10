import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr/toastr";

import { Breadcrumb, Nav } from "./menu";

export const renderer = (_: string) => {
  SpaRenderer.renderGlobalReact(<Nav />, document.getElementById("nav"));

  SpaRenderer.renderGlobalReact(<Breadcrumb />, document.getElementById("breadcrumb"));

  SpaRenderer.renderGlobalReact(
    <>
      <MessagesContainer containerId="global" />
    </>,
    document.getElementById("messages-container")
  );
};
