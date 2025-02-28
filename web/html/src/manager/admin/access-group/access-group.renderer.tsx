import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

// import { MessagesContainer } from "components/toastr";

import CreateAccessGroup from "./access-group";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    // <MessagesContainer />
    <CreateAccessGroup />,
    document.getElementById(id)
  );
};
