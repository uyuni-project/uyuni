import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";
// import { MessagesContainer } from "components/toastr";

import { AccessGroupList } from "./access-group-list";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    // <MessagesContainer />
    <AccessGroupList />,
    document.getElementById(id)
  );
};
